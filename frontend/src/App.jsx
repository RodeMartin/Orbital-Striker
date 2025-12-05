import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client/dist/sockjs';
import Stomp from 'stompjs';
import {
  Container,
  Box,
  Typography,
  Grid,
  Card,
  CardActionArea,
  Avatar,
  Paper,
  Button,
  Chip,
  Alert,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Snackbar,
  Fade,
} from '@mui/material';

import SportsSoccerIcon from '@mui/icons-material/SportsSoccer';
import PlayCircleFilledWhiteIcon from '@mui/icons-material/PlayCircleFilledWhite';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EmailIcon from '@mui/icons-material/Email';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents';

const PITCHES = [
  { id: 'classic', name: 'STADION', color: '#1b5e20' },
  { id: 'night', name: 'ESTI STADION', color: '#026353ff' },
  { id: 'street', name: 'UTCAI PÁRBAJ', color: '#303030ff' },
];

const MAX_DRAG_DISTANCE = 200;
const FORCE_MULTIPLIER = 0.25;

class Particle {
  constructor(x, y, color, speed, life) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.vx = (Math.random() - 0.5) * speed;
    this.vy = (Math.random() - 0.5) * speed;
    this.life = life;
    this.alpha = 1;
  }
  update() {
    this.x += this.vx;
    this.y += this.vy;
    this.life--;
    this.alpha = this.life / 60;
  }
  draw(ctx) {
    ctx.save();
    ctx.globalAlpha = this.alpha;
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.arc(this.x, this.y, Math.random() * 3 + 1, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
  }
}

const bgStyle = {
  minHeight: '100vh',
  backgroundImage:
    "url('https://images.unsplash.com/photo-1518091043644-c1d4457512c6?auto=format&fit=crop&w=1920&q=80')",
  backgroundSize: 'cover',
  backgroundPosition: 'center',
  color: 'white',
};

const glassStyle = {
  background: 'rgba(10,10,10,0.9)',
  backdropFilter: 'blur(18px)',
  borderRadius: 16,
  border: '1px solid rgba(255,255,255,0.08)',
  boxShadow: '0 18px 45px rgba(0,0,0,0.7)',
};

function App() {
  // --- ÁLLAPOTOK ---
  const [appState, setAppState] = useState('AUTH'); // AUTH | SELECT | GAME
  const [user, setUser] = useState(null);

  // --- LOGIN / REGISTER mód ---
  const [authMode, setAuthMode] = useState("REGISTER"); // REGISTER | LOGIN
  const [loginEmail, setLoginEmail] = useState("");
  const [loginUsername, setLoginUsername] = useState("");

  // auth
  const [regUsername, setRegUsername] = useState('');
  const [regEmail, setRegEmail] = useState('');
  const [authMsg, setAuthMsg] = useState(null);

  // csapatok / ligák DB-ből
  const [teams, setTeams] = useState([]);
  const [leagues, setLeagues] = useState([]);
  const [selectedLeague, setSelectedLeague] = useState('');
  const [teamsError, setTeamsError] = useState(null);
  const [loadingTeams, setLoadingTeams] = useState(false);

  const [selectedTeam, setSelectedTeam] = useState('');
  const [aiTeam, setAiTeam] = useState('');
  const [difficulty, setDifficulty] = useState('MEDIUM');
  const [selectedPitch, setSelectedPitch] = useState(PITCHES[0]);

  // game
  const [stompClient, setStompClient] = useState(null);
  const [score, setScore] = useState({ home: 0, away: 0 });
  const [currentTurn, setCurrentTurn] = useState('');
  const [gameOver, setGameOver] = useState(false);
  const [achievement, setAchievement] = useState(null);

  // rajzolás
  const canvasRef = useRef(null);
  const containerRef = useRef(null);
  const gameStateRef = useRef(null);
  const particlesRef = useRef([]);
  const dragRef = useRef({
    isDragging: false,
    startPos: null,
    currPos: null,
    id: null,
  });
  const requestRef = useRef(null);
  const imagesRef = useRef({});

  // --- REGISZTRÁCIÓ ---
  const handleRegister = () => {
    setAuthMsg(null);
    fetch('http://localhost:8080/api/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: regUsername, email: regEmail }),
    })
      .then((res) => res.json())
      .then((data) => {
        if (data.success) {
          setUser(data.user);
          setAppState('SELECT');
        } else {
          setAuthMsg(data.message || 'Regisztráció sikertelen.');
        }
      })
      .catch(() => setAuthMsg('Szerver hiba a regisztrációnál.'));
  };

  // --- LOGIN ---
const handleLogin = () => {
  setAuthMsg(null);
  fetch("http://localhost:8080/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      username: loginUsername,
      email: loginEmail
    }),
  })
    .then((res) => res.json())
    .then((data) => {
      if (data.success) {
        setUser(data.user);
        setAppState("SELECT");
      } else {
        setAuthMsg(data.message || "Rossz jelszó/felhasználónév!");
      }
    })
    .catch(() => setAuthMsg("Szerverhiba bejelentkezéskor."));
};

  // --- TEAMEK BETÖLTÉSE DB-BŐL (SELECT képernyőn) ---
  useEffect(() => {
    if (appState !== 'SELECT') return;

    setTeamsError(null);
    setLoadingTeams(true);

    fetch('http://localhost:8080/api/teams')
      .then((res) => {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
      })
      .then((data) => {
        const map = new Map();
        data.forEach((t) => {
          if (!map.has(t.id)) map.set(t.id, t);
        });
        const teamsArr = Array.from(map.values());

        setTeams(teamsArr);
        const uniqLeagues = [...new Set(teamsArr.map((t) => t.league))];
        setLeagues(uniqLeagues);

        if (!selectedLeague && uniqLeagues.length > 0) {
          setSelectedLeague(uniqLeagues[0]);
        }
        if (!selectedTeam && teamsArr.length > 0) {
          setSelectedTeam(teamsArr[0].id);
        }
        if (!aiTeam && teamsArr.length > 1) {
          setAiTeam(teamsArr[1].id);
        }
      })
      .catch((err) => {
        console.error('Hiba /api/teams hívásnál:', err);
        setTeamsError('Nem sikerült betölteni a csapatokat a szerverről.');
      })
      .finally(() => setLoadingTeams(false));
  }, [appState]);

  // --- LOGÓK ELŐTÖLTÉSE (korongokra) ---
  useEffect(() => {
    teams.forEach((team) => {
      if (!team.logoUrl) return;
      const img = new Image();
      img.src = team.logoUrl;
      imagesRef.current[team.id] = img;
    });
  }, [teams]);

  // --- WEBSOCKET + RENDER LOOP INDÍTÁS ---
  useEffect(() => {
  const animate = () => {
    if (appState === 'GAME' && gameStateRef.current) {
      drawGame(gameStateRef.current);

      particlesRef.current.forEach((p) => p.update());
      particlesRef.current = particlesRef.current.filter((p) => p.life > 0);
    }
    requestRef.current = requestAnimationFrame(animate);
  };

  requestRef.current = requestAnimationFrame(animate);
  return () => cancelAnimationFrame(requestRef.current);
}, [appState, selectedPitch]);

    // websocket
    useEffect(() => {
  const socket = new SockJS('http://localhost:8080/ws');
  const client = Stomp.over(socket);
  client.debug = () => {};

  client.connect(
    {},
    () => {
      console.log('WebSocket connected');

      client.subscribe('/topic/game-state', (message) => {
        const state = JSON.parse(message.body);

        if (
          gameStateRef.current &&
          (state.homeScore > gameStateRef.current.homeScore ||
            state.awayScore > gameStateRef.current.awayScore)
        ) {
          spawnParticles(400, 300, 100, '#FFD700');
          showAchievement('GÓÓÓL!', 'A stadion felrobban!');
        }

        gameStateRef.current = state;
        setScore({ home: state.homeScore, away: state.awayScore });
        setCurrentTurn(state.currentTurn);
        if (state.gameOver) setGameOver(true);
      });

      setStompClient(client);
    },
    (err) => {
      console.error('WS error:', err);
    }
  );

  return () => {
    if (client && client.connected) {
      client.disconnect();
    }
  };
}, []);

  useEffect(() => {
    const handleResize = () => {
      if (
        appState === 'GAME' &&
        canvasRef.current &&
        containerRef.current
      ) {
        canvasRef.current.width = containerRef.current.clientWidth;
        canvasRef.current.height = containerRef.current.clientHeight;
      }
    };
    window.addEventListener('resize', handleResize);
    handleResize();
    return () => window.removeEventListener('resize', handleResize);
  }, [appState]);

  function showAchievement(title, desc) {
  setAchievement({ title, desc });
  setTimeout(() => setAchievement(null), 3500);
}

function spawnParticles(x, y, count, color) {
  for (let i = 0; i < count; i++) {
    particlesRef.current.push(new Particle(x, y, color, 6, 60));
  }
}

  const handleStartGame = () => {
    if (!stompClient || !stompClient.connected) {
      alert('Nincs kapcsolat a szerverrel. Várj pár másodpercet, majd próbáld újra!');
      return;
    }
    if (!selectedTeam || !aiTeam) {
      alert('Válaszd ki a saját csapatod és az ellenfelet!');
      return;
    }

    const startRequest = {
      playerName: user?.username || 'Guest',
      playerTeamId: selectedTeam,
      aiTeamId: aiTeam,
      difficulty: difficulty,
    };

    stompClient.send('/app/start', {}, JSON.stringify(startRequest));
    setAppState('GAME');
    setGameOver(false);
    particlesRef.current = [];
  };

  // --- CANVAS INPUT ---
  const toScreen = (x, y) => {
    const c = canvasRef.current;
    if (!c) return { x, y };
    return { x: x * (c.width / 800), y: y * (c.height / 600) };
  };

  const toServer = (x, y) => {
    const c = canvasRef.current;
    return { x: x * (800 / c.width), y: y * (600 / c.height) };
  };

  const getMouse = (e) => {
    const rect = canvasRef.current.getBoundingClientRect();
    return { x: e.clientX - rect.left, y: e.clientY - rect.top };
  };

  const handleMouseDown = (e) => {
    if (
      appState !== 'GAME' ||
      !gameStateRef.current ||
      gameStateRef.current.currentTurn !== 'player' ||
      gameOver
    )
      return;

    const m = getMouse(e);
    const s = toServer(m.x, m.y);

    const disc = gameStateRef.current.discs.find((d) => {
      const dx = d.position.x - s.x;
      const dy = d.position.y - s.y;
      return (
        Math.sqrt(dx * dx + dy * dy) < d.radius + 30 &&
        d.id.startsWith('player')
      );
    });

    if (disc) {
      dragRef.current = {
        isDragging: true,
        startPos: toScreen(disc.position.x, disc.position.y),
        currPos: m,
        id: disc.id,
      };
    }
  };

  const handleMouseMove = (e) => {
    if (!dragRef.current.isDragging) return;
    const m = getMouse(e);
    const start = dragRef.current.startPos;
    let dx = m.x - start.x;
    let dy = m.y - start.y;
    const dist = Math.sqrt(dx * dx + dy * dy);
    if (dist > MAX_DRAG_DISTANCE) {
      const r = MAX_DRAG_DISTANCE / dist;
      dx *= r;
      dy *= r;
    }
    dragRef.current.currPos = { x: start.x + dx, y: start.y + dy };
  };

    const handleMouseUp = () => {
    if (!dragRef.current.isDragging) return;
    const { startPos, currPos, id } = dragRef.current;
    let vx = startPos.x - currPos.x;
    let vy = startPos.y - currPos.y;
    const c = canvasRef.current;
    const scale = 800 / c.width;
    const fx = vx * scale * FORCE_MULTIPLIER;
    const fy = vy * scale * FORCE_MULTIPLIER;

    if (Math.abs(vx) > 10 || Math.abs(vy) > 10) {
      stompClient.send(
        '/app/move',
        {},
        JSON.stringify({ playerId: id, vectorX: fx, vectorY: fy })
      );
    }

    dragRef.current = {
      isDragging: false,
      startPos: null,
      currPos: null,
      id: null,
     };
    };

  // --- RAJZOLÁS ---
    function drawGame(state) {
    const c = canvasRef.current;
    if (!c) return;
    const ctx = c.getContext('2d');

    const pitch = selectedPitch;
    const showNames = true;

    ctx.clearRect(0, 0, c.width, c.height);

    const sx = c.width / 800;
    const sy = c.height / 600;

    // pálya
    ctx.fillStyle = pitch.color;
    ctx.fillRect(0, 0, c.width, c.height);

    ctx.fillStyle = 'rgba(255,255,255,0.04)';
    for (let i = 0; i < c.width; i += 100 * sx) {
      ctx.fillRect(i, 0, 50 * sx, c.height);
    }

    ctx.strokeStyle = 'rgba(255,255,255,0.6)';
    ctx.lineWidth = 4 * sx;
    ctx.strokeRect(50 * sx, 50 * sy, 700 * sx, 500 * sy);

    ctx.beginPath();
    ctx.arc(400 * sx, 300 * sy, 70 * sx, 0, 2 * Math.PI);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(400 * sx, 50 * sy);
    ctx.lineTo(400 * sx, 550 * sy);
    ctx.stroke();

    const drawGoal = (x, y, isLeft) => {
      const gw = 40 * sx;
      const gh = 100 * sy;

      ctx.save();
      ctx.strokeStyle = 'rgba(255,255,255,0.8)';
      ctx.lineWidth = 3;

      ctx.beginPath();
      if (isLeft) ctx.rect(x - gw, y, gw, gh);
      else ctx.rect(x, y, gw, gh);
      ctx.fillStyle = 'rgba(255,255,255,0.15)';
      ctx.fill();
      ctx.stroke();
      ctx.restore();
    };
    drawGoal(50 * sx, 250 * sy, true);
    drawGoal(750 * sx, 250 * sy, false);

    particlesRef.current.forEach((p) => p.draw(ctx));

    // bábuk + logók
    if (state.discs) {
      state.discs.forEach((disc) => {
        const x = disc.position.x * sx;
        const y = disc.position.y * sy;
        const r = disc.radius * sx;

        ctx.save();
        ctx.shadowColor = 'rgba(0,0,0,0.7)';
        ctx.shadowBlur = 12;
        ctx.shadowOffsetY = 4;

        ctx.beginPath();
        ctx.arc(x, y, r, 0, 2 * Math.PI);
        ctx.fillStyle = disc.color || '#ffffff';
        ctx.fill();

        if (!disc.isBall) {
          let teamId = null;
          if (disc.id.includes('player')) teamId = selectedTeam;
          else if (disc.id.includes('ai')) teamId = aiTeam;

          const img = imagesRef.current[teamId];
          if (img) {
            ctx.save();
            ctx.beginPath();
            ctx.arc(x, y, r * 0.9, 0, Math.PI * 2);
            ctx.clip();
            ctx.drawImage(img, x - r, y - r, r * 2, r * 2);
            ctx.restore();
          }
        } else {
          ctx.fillStyle = 'rgba(0,0,0,0.3)';
          ctx.beginPath();
          ctx.arc(x + r * 0.3, y - r * 0.3, r * 0.25, 0, Math.PI * 2);
          ctx.fill();
        }

        ctx.shadowColor = 'transparent';
        ctx.lineWidth = dragRef.current.id === disc.id ? 4 : 2;
        ctx.strokeStyle =
          dragRef.current.id === disc.id
            ? '#FFD700'
            : 'rgba(255,255,255,0.8)';
        ctx.beginPath();
        ctx.arc(x, y, r, 0, 2 * Math.PI);
        ctx.stroke();

        if (showNames && !disc.isBall && disc.name) {
          ctx.font = `${12 * sx}px Roboto, Arial`;
          ctx.textAlign = 'center';
          ctx.lineWidth = 3;
          ctx.strokeStyle = 'rgba(0,0,0,0.8)';
          ctx.strokeText(disc.name, x, y - r - 6);
          ctx.fillStyle = 'white';
          ctx.fillText(disc.name, x, y - r - 6);
        }

        ctx.restore();
      });
    }

    const drag = dragRef.current;
    if (drag.isDragging && drag.startPos && drag.currPos) {
      const start = drag.startPos;
      const current = drag.currPos;
      const dist = Math.hypot(start.x - current.x, start.y - current.y);
      const power = Math.min(dist / MAX_DRAG_DISTANCE, 1);

      const aimX = start.x + (start.x - current.x);
      const aimY = start.y + (start.y - current.y);

      ctx.beginPath();
      ctx.moveTo(start.x, start.y);
      ctx.lineTo(current.x, current.y);
      ctx.strokeStyle = 'rgba(255,255,255,0.4)';
      ctx.lineWidth = 2;
      ctx.stroke();

      ctx.beginPath();
      ctx.moveTo(start.x, start.y);
      ctx.lineTo(aimX, aimY);
      const r = Math.floor(power * 255);
      const g = Math.floor((1 - power) * 180);
      ctx.strokeStyle = `rgb(${r},${g},0)`;
      ctx.lineWidth = 3 + power * 6;
      ctx.setLineDash([14, 8]);
      ctx.stroke();
      ctx.setLineDash([]);

      ctx.font = 'bold 14px Arial';
      ctx.fillStyle = 'white';
      ctx.fillText(`${Math.round(power * 100)}%`, current.x, current.y + 24);
    }
  };

  const homeTeam = teams.find((t) => t.id === selectedTeam);
  const awayTeam = teams.find((t) => t.id === aiTeam);

  if (appState === 'AUTH') {
    return (
      <Box sx={bgStyle}>
        <Container
          maxWidth="sm"
          sx={{
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Fade in={true}>
            <Paper sx={{ ...glassStyle, p: 5, textAlign: 'center' }}>
              <SportsSoccerIcon sx={{ fontSize: 80, color: '#4caf50', mb: 1 }} />
              {/* MODE SWITCH */}
<Box sx={{ display: 'flex', justifyContent: 'center', mb: 3, gap: 2 }}>
  <Button 
    onClick={() => setAuthMode("REGISTER")}
    variant={authMode === "REGISTER" ? "contained" : "outlined"}
    sx={{ color: 'white' }}
  >
    REGISZTRÁCIÓ
  </Button>

  <Button 
    onClick={() => setAuthMode("LOGIN")}
    variant={authMode === "LOGIN" ? "contained" : "outlined"}
    sx={{ color: 'white' }}
  >
    BEJELENTKEZÉS
  </Button>
</Box>

{authMode === "REGISTER" ? (
  <>
    <TextField
      fullWidth
      label="Felhasználónév"
      variant="filled"
      value={regUsername}
      onChange={(e) => setRegUsername(e.target.value)}
      sx={{ mb: 2, bgcolor: 'rgba(4, 2, 2, 0.9)', borderRadius: 1 }}
    />
    <TextField
      fullWidth
      label="Email Cím"
      variant="filled"
      value={regEmail}
      onChange={(e) => setRegEmail(e.target.value)}
      sx={{ mb: 3, bgcolor: 'rgba(4, 2, 2, 0.9)', borderRadius: 1 }}
    />

    <Button
      fullWidth
      size="large"
      variant="contained"
      startIcon={<EmailIcon />}
      onClick={handleRegister}
      sx={{
        py: 1.6,
        fontWeight: 'bold',
        fontSize: '1rem',
        background:
          'linear-gradient(45deg, #4caf50 0%, #81c784 50%, #a5d6a7 100%)',
      }}
    >
      Fiók Létrehozása
    </Button>
  </>
) : (
  <>
    <TextField
      fullWidth
      label="Felhasználónév"
      variant="filled"
      value={loginUsername}
      onChange={(e) => setLoginUsername(e.target.value)}
      sx={{ mb: 2, bgcolor: 'rgba(5, 3, 3, 0.9)', borderRadius: 1 }}
    />
    <TextField
      fullWidth
      label="Email Cím"
      variant="filled"
      value={loginEmail}
      onChange={(e) => setLoginEmail(e.target.value)}
      sx={{ mb: 3, bgcolor: 'rgba(5, 3, 3, 0.9)', borderRadius: 1 }}
    />

    <Button
      fullWidth
      size="large"
      variant="contained"
      startIcon={<EmailIcon />}
      onClick={handleLogin}
      sx={{
        py: 1.6,
        fontWeight: 'bold',
        fontSize: '1rem',
        background:
          'linear-gradient(45deg, #2196f3 0%, #64b5f6 50%, #bbdefb 100%)',
      }}
    >
      BEJELENTKEZÉS
    </Button>
  </>
)}

{authMsg && (
  <Alert severity="info" sx={{ mt: 3 }}>
    {authMsg}
  </Alert>
)}

            </Paper>
          </Fade>
        </Container>
      </Box>
    );
  }


  if (appState === 'SELECT') {
    const visibleTeams = teams.filter(
      (t) => !selectedLeague || t.league === selectedLeague
    );

    return (
      <Box sx={bgStyle}>
        <Container maxWidth="lg" sx={{ py: 4 }}>
          <Fade in={true}>
            <Paper sx={{ ...glassStyle, p: 4, minHeight: '90vh' }}>
              {/* felső sáv */}
              <Box
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  mb: 3,
                }}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <IconButton
                    onClick={() => setAppState('AUTH')}
                    sx={{ color: 'white' }}
                  >
                    <ArrowBackIcon />
                  </IconButton>
                  <Typography variant="h4" sx={{ fontWeight: '900' }}>
                    SETUP
                  </Typography>
                </Box>

                {user && (
                  <Chip
                    label={user.username}
                    sx={{
                      bgcolor: 'rgba(255,255,255,0.08)',
                      color: 'white',
                      borderRadius: 999,
                    }}
                  />
                )}
              </Box>

              {teamsError && (
                <Alert severity="error" sx={{ mb: 2 }}>
                  {teamsError}
                </Alert>
              )}
              {loadingTeams && (
                <Alert severity="info" sx={{ mb: 2 }}>
                  Csapatok betöltése...
                </Alert>
              )}

              {/* liga választó */}
              <Typography
                variant="subtitle2"
                sx={{ color: '#aaaaaa', letterSpacing: 2, mb: 1 }}
              >
                VÁLASSZ LIGÁT!
              </Typography>
              <Box sx={{ display: 'flex', gap: 1.5, mb: 3, flexWrap: 'wrap' }}>
                {leagues.map((league) => (
                  <Chip
                    key={league}
                    label={league}
                    clickable
                    onClick={() => setSelectedLeague(league)}
                    color={league === selectedLeague ? 'success' : 'default'}
                    variant={
                      league === selectedLeague ? 'filled' : 'outlined'
                    }
                    sx={{
                      borderRadius: 999,
                      color: 'white',
                      borderColor: 'rgba(255,255,255,0.3)',
                      fontWeight:
                        league === selectedLeague ? 'bold' : 'normal',
                    }}
                  />
                ))}
              </Box>

              <Grid container spacing={4}>
                {/* CSAPAT SLIDER */}
                <Grid item xs={12} md={7}>
                  <Typography
                    variant="subtitle2"
                    sx={{ color: '#aaaaaa', letterSpacing: 2, mb: 1 }}
                  >
                    VÁLASZD KI A CSAPATOD!
                  </Typography>
                  <Box
                    sx={{
                      display: 'flex',
                      gap: 2,
                      overflowX: 'auto',
                      pb: 2,
                      '&::-webkit-scrollbar': { height: 6 },
                      '&::-webkit-scrollbar-thumb': {
                        bgcolor: 'rgba(255,255,255,0.25)',
                        borderRadius: 999,
                      },
                    }}
                  >
                    {visibleTeams.map((team) => (
                      <Card
                        key={team.id}
                        sx={{
                          minWidth: 190,
                          bgcolor:
                            selectedTeam === team.id
                              ? 'rgba(41, 16, 201, 0.9)'
                              : 'rgba(203, 10, 10, 0.89)',
                          color: 'white',
                          border:
                            selectedTeam === team.id
                              ? '2px solid #322e19ff'
                              : '1px solid rgba(255,255,255,0.1)',
                          boxShadow:
                            selectedTeam === team.id
                              ? '0 0 25px rgba(255,215,0,0.4)'
                              : '0 10px 25px rgba(0,0,0,0.6)',
                          transform:
                            selectedTeam === team.id
                              ? 'translateY(-4px)'
                              : 'translateY(0)',
                          transition: 'all 0.2s ease',
                        }}
                      >
                        <CardActionArea
                          sx={{ p: 2, textAlign: 'center' }}
                          onClick={() => setSelectedTeam(team.id)}
                        >
                          <Box
                            sx={{
                              height: 90,
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              mb: 1,
                            }}
                          >
                            <img
                              src={team.logoUrl}
                              alt={team.name}
                              style={{
                                width: 90,
                                height: 90,
                                borderRadius: "50%",
                                objectFit: "cover",
                                backgroundColor: "black",
                              }}
                            />

                          </Box>
                          <Typography
                            variant="caption"
                            sx={{
                              textTransform: 'uppercase',
                              opacity: 0.7,
                            }}
                          >
                            {team.league}
                          </Typography>
                          <Typography
                            variant="subtitle1"
                            sx={{
                              mt: 0.5,
                              fontWeight: 'bold',
                              color: '#f5f5f5', // <-- mindig világos
                            }}
                          >
                            {team.name}
                          </Typography>
                        </CardActionArea>
                      </Card>
                    ))}
                  </Box>
                </Grid>

                {/* MATCH PREVIEW */}
                <Grid item xs={12} md={5}>
                  <Typography
                    variant="subtitle2"
                    sx={{ color: '#aaaaaa', letterSpacing: 2, mb: 1 }}
                  >
                    ELŐNÉZET
                  </Typography>
                  <Paper
                    sx={{
                      bgcolor: 'rgba(0,0,0,0.75)',
                      borderRadius: 4,
                      border: '1px solid rgba(255,255,255,0.08)',
                      p: 3,
                    }}
                  >
                    <Box
                      sx={{
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        mb: 3,
                      }}
                    >
                      {/* YOU */}
                      <Box sx={{ textAlign: 'center', flex: 1 }}>
                        <Avatar
                          src={homeTeam?.logoUrl}
                          sx={{
                            width: 72,
                            height: 72,
                            mx: "auto",
                            mb: 1,
                            borderRadius: "50%",
                            bgcolor: "black",
                            objectFit: "cover",
                          }}
                        />

                        <Typography
                          variant="caption"
                          sx={{ opacity: 0.7, textTransform: 'uppercase' }}
                        >
                          YOU
                        </Typography>
                        <Typography
                          variant="subtitle1"
                          sx={{ fontWeight: 'bold', color: '#ffffff' }}
                        >
                          {homeTeam?.name || '-'}
                        </Typography>
                      </Box>

                      <Typography
                        variant="h4"
                        sx={{ fontWeight: '900', mx: 2 }}
                      >
                        VS
                      </Typography>

                      {/* CPU */}
                      <Box sx={{ textAlign: 'center', flex: 1 }}>
                        <Avatar
                          src={awayTeam?.logoUrl}
                          sx={{
                            width: 72,
                            height: 72,
                            mx: "auto",
                            mb: 1,
                            borderRadius: "50%",
                            bgcolor: "black",
                            objectFit: "cover",
                          }}
                        />
                        <Typography
                          variant="caption"
                          sx={{ opacity: 0.7, textTransform: 'uppercase' }}
                        >
                          CPU
                        </Typography>
                        <Typography
                          variant="subtitle1"
                          sx={{ fontWeight: 'bold', color: '#ffffff' }}
                        >
                          {awayTeam?.name || '-'}
                        </Typography>
                      </Box>
                    </Box>

                    {/* QUICK SETTINGS */}
                    <Grid container spacing={2}>
                      <Grid item xs={12}>
                        <FormControl fullWidth size="small">
                          <InputLabel sx={{ color: '#ccc' }}>
                            ELLENFÉL
                          </InputLabel>
                          <Select
                            value={aiTeam}
                            label="OPPONENT"
                            onChange={(e) => setAiTeam(e.target.value)}
                            sx={{
                              color: 'white',
                              '.MuiOutlinedInput-notchedOutline': {
                                borderColor: '#555',
                              },
                            }}
                          >
                            {teams
                              .filter((t) => t.id !== selectedTeam)
                              .map((t) => (
                                <MenuItem key={t.id} value={t.id}>
                                  {t.name}
                                </MenuItem>
                              ))}
                          </Select>
                        </FormControl>
                      </Grid>
                      <Grid item xs={6}>
                        <FormControl fullWidth size="small">
                          <InputLabel sx={{ color: '#ccc' }}>
                            NEHÉZSÉG
                          </InputLabel>
                          <Select
                            value={difficulty}
                            label="DIFFICULTY"
                            onChange={(e) => setDifficulty(e.target.value)}
                            sx={{
                              color: 'white',
                              '.MuiOutlinedInput-notchedOutline': {
                                borderColor: '#555',
                              },
                            }}
                          >
                            <MenuItem value="EASY">KEZDŐ</MenuItem>
                            <MenuItem value="MEDIUM">PROFESSZIONÁLIS</MenuItem>
                            <MenuItem value="HARD">VILÁGKLASSZIS</MenuItem>
                          </Select>
                        </FormControl>
                      </Grid>
                      <Grid item xs={6}>
                        <FormControl fullWidth size="small">
                          <InputLabel sx={{ color: '#ccc' }}>
                            PÁLYA
                          </InputLabel>
                          <Select
                            value={selectedPitch.id}
                            label="PITCH TYPE"
                            onChange={(e) =>
                              setSelectedPitch(
                                PITCHES.find((p) => p.id === e.target.value)
                              )
                            }
                            sx={{
                              color: 'white',
                              '.MuiOutlinedInput-notchedOutline': {
                                borderColor: '#555',
                              },
                            }}
                          >
                            {PITCHES.map((p) => (
                              <MenuItem key={p.id} value={p.id}>
                                {p.name}
                              </MenuItem>
                            ))}
                          </Select>
                        </FormControl>
                      </Grid>
                    </Grid>
                  </Paper>
                </Grid>
              </Grid>

              {/* KICK OFF BUTTON */}
              <Box
                sx={{
                  mt: 4,
                  display: 'flex',
                  justifyContent: 'flex-end',
                }}
              >
                <Button
                  variant="contained"
                  size="large"
                  onClick={handleStartGame}
                  startIcon={<PlayCircleFilledWhiteIcon />}
                  sx={{
                    px: 7,
                    py: 1.8,
                    fontWeight: 'bold',
                    fontSize: '1rem',
                    background:
                      'linear-gradient(45deg, #43a047 0%, #66bb6a 40%, #a5d6a7 100%)',
                  }}
                >
                  KEZDŐRÚGÁS
                </Button>
              </Box>
            </Paper>
          </Fade>
        </Container>

        {/* Achievement toast */}
        <Snackbar
          open={Boolean(achievement)}
          autoHideDuration={3500}
          anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
        >
          <Alert
            icon={<EmojiEventsIcon fontSize="inherit" />}
            severity="success"
            variant="filled"
            sx={{ bgcolor: '#2e7d32', color: 'white' }}
          >
            <Typography fontWeight="bold">
              {achievement?.title}
            </Typography>
            {achievement?.desc}
          </Alert>
        </Snackbar>
      </Box>
    );
  }

  return (
    <Box
      sx={{
        bgcolor: '#050505',
        color: 'white',
        width: '100vw',
        height: '100vh',
        overflow: 'hidden',
        position: 'relative',
      }}
    >
      {/* GOAL POPUP */}
<Snackbar
  open={Boolean(achievement)}
  autoHideDuration={3000}
  anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
>
  <Alert
    severity="success"
    variant="filled"
    icon={<EmojiEventsIcon />}
    sx={{
      bgcolor: '#4caf50',
      color: 'white',
      fontSize: '1.2rem',
      px: 2,
    }}
  >
    <strong>{achievement?.title}</strong> — {achievement?.desc}
  </Alert>
</Snackbar>

      {/* SCOREBOARD */}
      <Box
        sx={{
          position: 'absolute',
          top: 16,
          left: '50%',
          transform: 'translateX(-50%)',
          display: 'flex',
          gap: 2,
          zIndex: 10,
        }}
      >
        <Paper
          sx={{
            ...glassStyle,
            px: 3,
            py: 1,
            borderRadius: 999,
            borderLeft: `4px solid ${homeTeam?.color || '#2e7d32'}`,
            display: 'flex',
            alignItems: 'center',
            gap: 1.5,
          }}
        >
             <Avatar
              src={homeTeam?.logoUrl}
              sx={{
                width: 40,
                height: 40,
                borderRadius: "50%",
                bgcolor: "black",
                objectFit: "cover",
              }}
            />

          <Typography sx={{ fontWeight: 'bold', fontSize: 24 }}>
            {score.home}
          </Typography>
        </Paper>

        <Paper
          sx={{
            ...glassStyle,
            px: 2.5,
            py: 1,
            borderRadius: 999,
            textAlign: 'center',
          }}
        >
          <Typography
            variant="caption"
            sx={{ textTransform: 'uppercase', opacity: 0.7 }}
          >
            TURN
          </Typography>
          <Typography sx={{ fontWeight: 'bold' }}>
            {currentTurn === 'player' ? 'YOU' : 'CPU'}
          </Typography>
        </Paper>

        <Paper
          sx={{
            ...glassStyle,
            px: 3,
            py: 1,
            borderRadius: 999,
            borderRight: `4px solid ${awayTeam?.color || '#263238'}`,
            display: 'flex',
            alignItems: 'center',
            gap: 1.5,
          }}
        >
          <Typography sx={{ fontWeight: 'bold', fontSize: 24 }}>
            {score.away}
          </Typography>
          <Avatar
            src={awayTeam?.logoUrl}
            sx={{
                width: 40,
                height: 40,
                borderRadius: "50%",
                bgcolor: "black",
                objectFit: "cover",
              }}
          />
        </Paper>
      </Box>

      {/* QUIT BUTTON */}
      <Button
        onClick={() => setAppState('SELECT')}
        sx={{
          position: 'absolute',
          bottom: 16,
          left: 16,
          zIndex: 10,
          color: 'white',
          textTransform: 'none',
        }}
        startIcon={<ArrowBackIcon />}
      >
        Vissza a menübe
      </Button>

      {/* MATCH ENDED MODAL */}
      {gameOver && (
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            bgcolor: 'rgba(0,0,0,0.75)',
            zIndex: 20,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          <Paper
            sx={{
              ...glassStyle,
              p: 4,
              textAlign: 'center',
              border: '2px solid #FFD700',
            }}
          >
            <Typography
              variant="h3"
              sx={{ fontWeight: '900', color: '#FFD700', mb: 1 }}
            >
              VÉGE A MÉRKŐZÉSNEK!
            </Typography>
            <Typography variant="h5" sx={{ mb: 2 }}>
              {score.home > score.away
                ? 'GYŐZELEM'
                : score.home < score.away
                ? 'VERESÉG'
                : 'DÖNTETLEN'}
            </Typography>
            <Typography
              variant="h2"
              sx={{
                fontFamily: 'monospace',
                fontWeight: 'bold',
                mb: 3,
              }}
            >
              {score.home} - {score.away}
            </Typography>
            <Button
              variant="contained"
              onClick={() => {
                setGameOver(false);
                setAppState('SELECT');
              }}
              sx={{
                px: 5,
                py: 1.2,
                fontWeight: 'bold',
                background:
                  'linear-gradient(45deg, #43a047 0%, #66bb6a 50%, #a5d6a7 100%)',
              }}
            >
              Vissza a főmenübe
            </Button>
          </Paper>
        </Box>
      )}

      {/* CANVAS */}
      <Box
        ref={containerRef}
        sx={{ width: '100%', height: '100%', bgcolor: '#0b1e0b' }}
      >
        <canvas
          ref={canvasRef}
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onMouseLeave={handleMouseUp}
          style={{ display: 'block', width: '100%', height: '100%' }}
        />
      </Box>
    </Box>
  );
}

export default App;
