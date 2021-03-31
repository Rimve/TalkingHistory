import React from "react";
import Modal from 'react-bootstrap/Modal';
import '../styles/EditModal.css';
import ComponentLoadingComponent from "./ComponentLoadingComponent";
import {getCharacterPicture} from "../services/Utilities";
import {
    getCharDescriptionRef,
    getCharImageFileRef,
    getCharImageStorageRef, getNodeOfIdRef
} from "../services/DatabaseService";
import * as BiIcons from "react-icons/bi";

class EditCharModal extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            name: this.props.name,
            show: this.props.show,
            description: this.props.description,
            showImage: true,
            showUpload: false,
            showLoad: true,
            newName: null,
            file: null
        };
    }

    showUploadCallback = (data) => {
        this.setState({showUpload: data});
    };

    handleNameChange = (change) => {
        this.setState({newName: change.target.value})
    };

    handleDescriptionChange = (change) => {
        this.setState({description: change.target.value})
    };

    handleClose = () => {
        this.setState({ show: false });
        this.props.showCallBack(false);
    };

    handleSubmit = () => {
        if (this.state.newName !== null) {
            this.setState({name: this.state.newName}, () => {
                this.updateCharacter(this.state.name)
            })
        }

        if (this.state.name !== null) {
            this.updateCharacter(this.state.name)
        }
        else {
            alert("Character must have a name")
        }
    };

    handleRemove = () => {
        this.removeImage()
    };

    fileChangeHandler = (event) => {
        if (event.target.files[0].type.includes("jpeg")) {
            this.setImage(event.target.files[0])
        } else {
            event.target.value = null;
            alert("Photo file format must be JPG")
        }
    };

    setImage(image) {
        const fileReader = new FileReader();
        fileReader.onload = () => {
            document.getElementById('character-edit-container')
                .setAttribute('style', 'background-image: url('+fileReader.result+')');
            document.getElementById('add-image')
                .setAttribute('style', 'display: none');
            this.setState({file: image, showImage: false})
        };
        fileReader.readAsDataURL(image);
    }

    removeImage() {
        document.getElementById('character-edit-container')
            .setAttribute('style', 'background-image: none');
        document.getElementById('add-image')
            .setAttribute('style', 'display: flex');
        this.setState({file: null, showImage: false})
    }

    showName() {
        if (this.state.name === null) {
            return (
                <div className='name-width'>
                    <textarea className='form-control' placeholder='Enter name' rows='1' onChange={this.handleNameChange} />
                </div>
            )
        }
        else {
            return (
                <div className='character-text'>
                    {this.state.name}
                </div>
            )
        }
    }

    showImage() {
        if (this.state.name === null) {
            return (
                <>
                    <BiIcons.BiImageAdd className='icon-add' id='add-image' />
                    <input type='file' name='file' ref={(ref) => this.upload = ref} accept='.jpg'
                           onChange={this.fileChangeHandler} style={{display: "none"}} />
                </>
            )
        }
        else {
            return (
                <>
                    <div className='character-picture' id={'edit-loader'}>
                        {this.loadComponent()}
                    </div>
                    <BiIcons.BiImageAdd className='icon-add' id='add-image' style={{display: "none"}} />
                    <input type='file' name='file' ref={(ref) => this.upload = ref} accept='.jpg'
                           onChange={this.fileChangeHandler} style={{display: "none"}} />
                    { getCharacterPicture(this.state.name,'character-edit-container',
                        'edit-loader', this.state.showImage) }
                </>
            )
        }
    }

    showDescription() {
        if (this.state.description === null) {
            return (
                <textarea placeholder='Enter characters description'
                          onChange={this.handleDescriptionChange}
                          className='form-control' rows='3' />
            )
        }
        else {
            return (
                <textarea value={this.state.description}
                          onChange={this.handleDescriptionChange}
                          className='form-control' rows='3' />
            )
        }
    }

    updateCharacter(name) {
        const {file} = this.state
        const {description} = this.state

        if (name !== null) {
            if (file === null) {
                getCharImageFileRef(name).once('value').then((snapshot) => {
                    let results = snapshot.val();
                    if (results !== null) {
                        getCharImageStorageRef(name, results).delete().then(() => {
                            getCharImageFileRef(name).remove().then()
                                .catch((e) => {
                                    console.log(e)
                                });
                        }).catch((e) => {
                            console.log(e)
                        });
                    }
                }).catch((e) => {
                    console.log(e)
                });
            }
            else {
                let fileName = 'picture';
                getCharImageStorageRef(name, fileName).put(file).then((snapshot) => {
                    getCharImageFileRef(name).set(fileName).then(() => {
                        getCharacterPicture(this.state.name,'character-edit-container', 'edit-loader', true)
                    });
                });
            }

            getNodeOfIdRef(name, 0).set("Node start")
                .catch((e) => {
                    console.log(e)
                });

            getCharDescriptionRef(name).set(description)
                .catch((e) => {
                    console.log(e)
                })
            this.props.showCallBack(false);
        }
    }

    loadComponent() {
        return (
            this.state.showLoad ? <ComponentLoadingComponent /> : null
        )
    }

    render() {
        return (
            <>
                <Modal show={this.state.show} onHide={this.handleClose} centered>
                    <Modal.Header closeButton>
                        <Modal.Title>Character Editor</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <div className='align-items-center'>
                            <div className="character-banner justify-center">
                                {this.showName()}
                            </div>
                            <div className='d-flex justify-center'>
                                <div className='character-picture pointer' id={'character-edit-container'}
                                     onClick={(e) => this.upload.click() }>
                                    {this.showImage()}
                                </div>
                            </div>
                            <div className='d-flex justify-center'>
                                <button className='button cancel-btn'
                                        onClick={() => {this.handleRemove()}}>Remove</button>
                            </div>
                            <div className="align-items-center margin-top">
                                {this.showDescription()}
                            </div>
                        </div>
                    </Modal.Body>
                    <Modal.Footer>
                        <button className='modal-btn cancel-btn' onClick={this.handleClose} >
                            <b>Cancel</b>
                        </button>
                        <button className='modal-btn' onClick={this.handleSubmit}>
                            <b>Submit</b>
                        </button>
                    </Modal.Footer>
                </Modal>
            </>
        );
    }
}

export default EditCharModal;