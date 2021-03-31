import React from "react";
import Modal from 'react-bootstrap/Modal';
import '../styles/EditModal.css';

class UploadModalComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            show: this.props.show,
            fileType: this.props.type,
            file: null
        };
    }

    handleClose = () => {
        this.setState({ show: false });
        this.props.showUploadCallBack(false);
    };

    handleSubmit = () => {
        this.props.uploadCallBack(this.state.file);
        this.props.showUploadCallBack(false);
    };

    onChangeHandler = (event) => {
        if (this.handleAcceptableFile() === '.mp3') {
            if (event.target.files[0].type.includes("mpeg")) {
                this.setState({file: event.target.files[0]})
            }
            else {
                event.target.value = null;
                alert("Audio file format must be MP3")
            }
        }
        if (this.handleAcceptableFile() === '.jpg') {
            if (event.target.files[0].type.includes("jpeg")) {
                this.setState({file: event.target.files[0]})
            }
            else {
                event.target.value = null;
                alert("Photo file format must be JPG")
            }
        }
    };

    handleAcceptableFile() {
        if (this.state.fileType === 'audio')
            return '.mp3';
        if (this.state.fileType === 'picture')
            return '.jpg'
    }

    render() {
        return (
            <>
                <Modal show={this.state.show} onHide={this.handleClose} centered>
                    <Modal.Header closeButton>
                        <Modal.Title>Upload file</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <input type='file' name='file' accept={this.handleAcceptableFile()} onChange={this.onChangeHandler}/>
                    </Modal.Body>
                    <Modal.Footer>
                        <button className='modal-btn cancel-btn' onClick={this.handleClose} >
                            <b>Cancel</b>
                        </button>
                        <button className='modal-btn' onClick={this.handleSubmit}>
                            <b>Upload</b>
                        </button>
                    </Modal.Footer>
                </Modal>
            </>
        );
    }
}

export default UploadModalComponent;