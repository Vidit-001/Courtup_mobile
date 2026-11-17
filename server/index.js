const express = require('express');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());

let rooms = [
  { id: 1, sport: 'Cricket', location: 'Sector 62, Noida', time: '6:00 PM', players: 2, maxPlayers: 11, host: 'Vidit', members: ['Vidit', 'Rahul'], pending: [] },
  { id: 2, sport: 'Basketball', location: 'Sector 18, Noida', time: '7:00 PM', players: 1, maxPlayers: 10, host: 'Rahul', members: ['Rahul'], pending: [] },
  { id: 3, sport: 'Football', location: 'Vaishali, Ghaziabad', time: '5:30 PM', players: 1, maxPlayers: 14, host: 'Arjun', members: ['Arjun'], pending: [] },
  { id: 4, sport: 'Table Tennis', location: 'Sector 63, Noida', time: '8:00 PM', players: 1, maxPlayers: 4, host: 'Priya', members: ['Priya'], pending: [] },
];

app.get('/api/rooms', (req, res) => res.json(rooms));

app.post('/api/rooms', (req, res) => {
  const { sport, location, time, maxPlayers, host } = req.body;
  const newRoom = {
    id: rooms.length + 1,
    sport, location, time,
    maxPlayers: parseInt(maxPlayers),
    players: 1,
    host,
    members: [host],
    pending: [],
  };
  rooms.push(newRoom);
  res.status(201).json(newRoom);
});

app.post('/api/rooms/:id/request', (req, res) => {
  const room = rooms.find(r => r.id === parseInt(req.params.id));
  if (!room) return res.status(404).json({ error: 'Room not found' });
  const { username } = req.body;
  if (room.members.includes(username)) return res.status(400).json({ error: 'Already in room' });
  if (room.pending.includes(username)) return res.status(400).json({ error: 'Request already sent' });
  if (room.players >= room.maxPlayers) return res.status(400).json({ error: 'Room is full' });
  room.pending.push(username);
  res.json({ message: 'Request sent' });
});

app.post('/api/rooms/:id/accept', (req, res) => {
  const room = rooms.find(r => r.id === parseInt(req.params.id));
  if (!room) return res.status(404).json({ error: 'Room not found' });
  const { username, requester } = req.body;
  if (room.host !== username) return res.status(403).json({ error: 'Only host can accept' });
  room.pending = room.pending.filter(p => p !== requester);
  room.members.push(requester);
  room.players += 1;
  res.json(room);
});

app.post('/api/rooms/:id/reject', (req, res) => {
  const room = rooms.find(r => r.id === parseInt(req.params.id));
  if (!room) return res.status(404).json({ error: 'Room not found' });
  const { username, requester } = req.body;
  if (room.host !== username) return res.status(403).json({ error: 'Only host can reject' });
  room.pending = room.pending.filter(p => p !== requester);
  res.json(room);
});

app.post('/api/rooms/:id/remove', (req, res) => {
  const room = rooms.find(r => r.id === parseInt(req.params.id));
  if (!room) return res.status(404).json({ error: 'Room not found' });
  const { username, target } = req.body;
  if (room.host !== username) return res.status(403).json({ error: 'Only host can remove' });
  if (target === room.host) return res.status(400).json({ error: 'Cannot remove host' });
  room.members = room.members.filter(m => m !== target);
  room.players -= 1;
  res.json(room);
});

app.listen(5000, () => console.log('Server running on port 5000'));