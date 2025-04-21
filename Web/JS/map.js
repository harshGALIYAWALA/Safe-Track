
// Firebase imports
import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-app.js";
import { getDatabase, ref, onValue } from "https://www.gstatic.com/firebasejs/11.6.0/firebase-database.js";


const firebaseConfig = {
    apiKey: "AIzaSyBEUUYzfnjqB4-WybPcCR6Pevh_k7bP4VU",
    authDomain: "safe-track-5d64f.firebaseapp.com",
    databaseURL: "https://safe-track-5d64f-default-rtdb.firebaseio.com",
    projectId: "safe-track-5d64f",
    storageBucket: "safe-track-5d64f.firebasestorage.app",
    messagingSenderId: "549537937946",
    appId: "1:549537937946:web:ad0860361ea0cd43002fa3",
    measurementId: "G-XG51YB91FV"
};

 // Initialize Firebase
const app = initializeApp(firebaseConfig);
const database = getDatabase(app);

const MAP_API_KEY = "fIVoqc2T9Nh7Y7NIz9r9";

const map = new maptilersdk.Map({
    container: 'map', // container id
    style: 'https://api.maptiler.com/maps/streets/style.json?key=fIVoqc2T9Nh7Y7NIz9r9', // style URL
    center: [72.818671, 21.181005], // starting position [lng, lat] - Note the lng, lat order for web mapping
    zoom: 12.0 // starting zoom
});

// fetching datat from firebase
// Realtime marker tracking
let marker = null;
const userIdToTrack = "HKgO2l5UWgggAUrp6BgIqPSGKgl2";
const liveLocationRef = ref(database, `user/${userIdToTrack}/liveLocation`);

onValue(liveLocationRef, (snapshot) => {
    console.log(`the value is = ${snapshot.val()}`);  // Log the entire value
    const data = snapshot.val();
    if (data) {
        const { latitude, longitude } = data;
        console.log("Latitude:", latitude, "Longitude:", longitude);
        if (marker) {
            marker.setLngLat([longitude, latitude]);
        } else {
            marker = new maptilersdk.Marker({ color: 'red' })
                .setLngLat([longitude, latitude])
                .addTo(map);
        }
        map.setCenter([longitude, latitude]); // Optional
    } else {
        console.log("No live location data available.");
    }
});
