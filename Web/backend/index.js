const mongoose = require("mongoose");
const express = require("express");
const connectDB = require("./config");
const session = require("./models/Session");

const app = express();
app.use(express.json());

connectDB();

//route 
app.get("/", (req, res) => {
    res.send("API is running....");
});

app.post("/api/startSession", async (req, res) => {
    try {
        const { uid, location, createdAt } = req.body;
        // Check if user already exists
        const existingSession = await session.findOne({ uid });
        if (existingSession) {
            existingSession.location = location;
            existingSession.createdAt = createdAt;
            await existingSession.save();
            res.status(200).json({ message: "Location updated" });
        } else {
            const newSession = new session({
                uid,
                location,
                createdAt
            });
            await newSession.save();
            res.status(201).json({ message: "Session created" });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({
            success: false,
            message: "Server error",
            error: error.message
        });
    }
});

// GET /sessions
app.get("/sessions", async (req, res) => {
    const sessions = await session.find();
    res.json(sessions);
});

app.listen(5000, '0.0.0.0', () => {
    console.log("server has started on port 5000");
});