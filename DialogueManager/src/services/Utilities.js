import {getCharImageFileRef, getCharImageStorageRef} from "./DatabaseService";

export function getCharacterPicture(charName, charContainer, loaderContainer, show) {
    if (show) {
        getCharImageFileRef(charName).once('value').then((snapshot) => {
            let results = snapshot.val();
            if (results !== null) {
                getCharImageStorageRef(charName, results).getDownloadURL().then((url) => {
                    if (charContainer !== null) {
                        document.getElementById(charContainer)
                            .setAttribute('style', 'background-image: url(' + url + ')');
                    }
                    if (document.getElementById(loaderContainer) !== null) {
                        document.getElementById(loaderContainer).setAttribute('style', 'visibility: collapse');
                    }
                }).catch((e) => {
                    console.log(e)
                });
            } else {
                setErrorImage(charContainer, loaderContainer)
            }
        }).catch((e) => {
            setErrorImage(charContainer, loaderContainer)
        });
    }
}

function setErrorImage(charContainer, loaderContainer) {
    if (document.getElementById(charContainer) !== null) {
        document.getElementById(charContainer)
            .setAttribute('style', 'background-image: url(https://firebasestorage.googleapis.com/v0/b/talkinghistory.appspot.com/o/error.png?alt=media&token=f6195b38-4c88-41bb-8873-8ee97fed783d)');
    }
    if (document.getElementById(loaderContainer) !== null) {
        document.getElementById(loaderContainer).setAttribute('style', 'visibility: collapse');
    }
}