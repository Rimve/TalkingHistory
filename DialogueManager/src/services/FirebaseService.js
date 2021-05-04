import firebase from "firebase/app";
import "firebase/database";
import {ROLES} from "../data/Roles";

export function initialize() {
    let firebaseConfig = {
        apiKey: process.env.REACT_APP_FIREBASE_KEY,
        authDomain: process.env.REACT_APP_AUTH_DOMAIN,
        databaseURL: process.env.REACT_APP_DATABASE_URL,
        projectId: "talkinghistory",
        storageBucket: process.env.REACT_APP_APP_STORAGE_BUCKET,
        messagingSenderId: process.env.REACT_APP_APP_SENDER_ID,
        appId: process.env.REACT_APP_API_ID,
        measurementId: process.env.REACT_APP_APP_MEASUREMENT_ID
    };
    // Initialize Firebase
    if (!firebase.apps.length) {
        firebase.initializeApp(firebaseConfig);
    } else {
        firebase.app();
    }
}

export function getNodesRef() {
    return firebase.database().ref("nodes");
}

export function getNodeOfIdRef(name, id) {
    return firebase.database().ref("nodes/" + name + "/" + id);
}

export function getTargetNodeOfIndex(name, srcNode, index) {
    return firebase.database().ref("adjacencies/" + name + "/" + srcNode + "/" + index);
}

export function getDstNode(name, srcNode) {
    return firebase.database().ref("adjacencies/" + name + "/" + srcNode);
}

export function getCharNodeRef(name) {
    return firebase.database().ref("nodes").child(name);
}

export function getCharQuestionsRef(name) {
    return firebase.database().ref("questions").child(name);
}

export function getCharQuestionOfIdRef(name, id) {
    return firebase.database().ref("questions/" + name + "/" + id);
}

export function getCharAdjRef(name) {
    return firebase.database().ref("adjacencies").child(name);
}

export function getUndefinedWordsRef(name) {
    return firebase.database().ref("undefined").child(name);
}

export function getCharAudioStorageRef(name, fileName) {
    return firebase.storage().ref(name).child("audio").child(fileName);
}

export function getCharImageStorageRef(name, fileName) {
    return firebase.storage().ref(name).child(fileName+".jpg");
}

export function getStorageRef(name) {
    return firebase.storage().ref(name);
}

export function getCharAudioFileRef(name, nodeId) {
    return firebase.database().ref("files").child(name).child("audio").child(nodeId);
}

export function getCharImageFileRef(name) {
    return firebase.database().ref("files").child(name).child("image");
}

export function getCharFilesRef(name) {
    return firebase.database().ref("files").child(name);
}

export function getCharDescriptionRef(name) {
    return firebase.database().ref("files").child(name).child("description");
}

export function getWordSimilaritiesRef() {
    return firebase.database().ref("similarities");
}

export function getUndefinedWordRef() {
    return firebase.database().ref("undefined");
}

export function getUserRoleRef(uuid) {
    return firebase.database().ref("roles").child(uuid)
}

export function getRolesRef() {
    return firebase.database().ref("roles")
}

export function firebaseAuthRef() {
    return firebase.auth()
}

export function getCurrentUserRole() {
    return firebase.database().ref("roles")
        .child(firebase.auth().currentUser.uid)
        .child("role")
}

export function loginUser(email, password) {
    firebase.auth().signInWithEmailAndPassword(email, password)
        .catch(function (error) {
            console.log(error.code);
            console.log(error.message);
        })
}

export function createUser(email, password) {
    firebase.auth().createUserWithEmailAndPassword(email, password)
        .then((userCred) => {
            getUserRoleRef(userCred.user.uid).child("role").set(ROLES.USER)
            getUserRoleRef(userCred.user.uid).child("email").set(email)
        })
        .catch(function (error) {
            console.log(error.code);
            console.log(error.message);
        })
}

export function logoutUser() {
    firebase.auth().signOut().catch(function (error) {
        console.log(error.code);
        console.log(error.message);
    });
}