const mongoose = require("mongoose");

const SessionSchema = new mongoose.Schema({
    uid: {
        type: String,
        required: true,
        unique: true
    },
    location: {
        latitude: Number,
        longitude: Number
    },
    createdAt: {
        date: String,
        time: String
    }
});

const Session = mongoose.model('Session', SessionSchema);

module.exports = Session;