import React from "react";
import Modal from 'react-bootstrap/Modal';
import '../styles/EditModal.css';
import AlertMassage from "./AlertMessage";
import {getWordSimilaritiesRef} from "../services/FirebaseService";

class CreateCategoryModal extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            show: this.props.show,
            newCategory: null,
            message: null,
            showAlert: false
        };
    }

    showAlertCallback = (data) => {
        this.setState({showAlert: data});
    };

    handleClose = () => {
        this.setState({ show: false });
        this.props.showCallBack(false);
    };

    handleSubmit = () => {
        if (this.state.newCategory !== null && this.state.newCategory.length > 0) {
            getWordSimilaritiesRef().child(this.state.newCategory).child("0").set("Nauja")
                .then(() => {
                    this.props.showCallBack(false);
                    this.props.addCatCallBack(this.state.newCategory);
                })
                .catch(() => {
                    this.setState({
                        message: "Something went wrong, try again",
                        showAlert: true
                    })
                })
        }
        else {
            this.setState({
                message: "Category name must be at least 1 character long",
                showAlert: true
            })
        }
    };

    handleTextChange = (change) => {
        this.setState({newCategory: change.target.value})
    };

    showAlert(message) {
        return (
            <AlertMassage message={message}
                          severity={"warning"}
                          show={this.state.showAlert}
                          showAlert={this.showAlertCallback} />
        )
    }

    render() {
        return (
            <>
                <Modal show={this.state.show} onHide={this.handleClose} centered>
                    <Modal.Header closeButton>
                        <Modal.Title>Create New Answer Category</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <textarea placeholder={"Enter name of the new category"}
                                  onChange={this.handleTextChange} className='form-control' rows='1' />
                    </Modal.Body>
                    <Modal.Footer>
                        <div/>
                        <div>
                            <button className='modal-btn cancel-btn' onClick={this.handleClose} >
                                <b>Cancel</b>
                            </button>
                            <button className='modal-btn' onClick={this.handleSubmit}>
                                <b>Submit</b>
                            </button>
                        </div>
                    </Modal.Footer>
                </Modal>
                {this.state.showAlert ? this.showAlert(this.state.message) : null}
            </>
        );
    }
}

export default CreateCategoryModal;