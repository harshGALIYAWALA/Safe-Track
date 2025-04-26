const mongoose = require("mongoose");

const sessionSchema = new mongoose.Schema({
    name: String, 
    Location:{
        lat: Number,
        lng: Number
    },
    createdAt:{
        date: {
            type: String,
            default: () => new Date().toLocaleDateString()
        },
        time: {
            type: String,
            default: () => new Date().toLocaleTimeString()
        }
    }
});

module.exports = mongoose.model("Session", sessionSchema);