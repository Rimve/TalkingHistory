import React from "react";
import Modal from 'react-bootstrap/Modal';
import '../styles/EditModal.css';

class EditModalComponent extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            show: this.props.show,
            nodeToEdit: this.props.node
        };
    }

    handleClose = () => {
        this.setState({ show: false });
        this.props.showCallBack(false);
    };

    handleSubmit = () => {
        this.props.editCallBack(this.state.nodeToEdit);
        this.props.showCallBack(false);
    };

    handleTextChange = (change) => {
        let newNode = this.state.nodeToEdit;
        newNode.data.scratch = change.target.value;
        this.setState({
            nodeToEdit: newNode
        })
    };


    handleCheckBoxChange = (change) => {
        let newNode = this.state.nodeToEdit;
        newNode.isQuestion = change.target.checked;
        this.setState({nodeToEdit: newNode})
    };

    handleDefaultValue() {
        if (this.state.nodeToEdit.data.scratch.includes("empty"))
            return "";
        else
            return this.state.nodeToEdit.data.scratch
    }

    render() {
        return (
            <>
                <Modal show={this.state.show} onHide={this.handleClose} centered>
                    <Modal.Header closeButton>
                        <Modal.Title>Dialogue entry</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <label>Is question: </label>
                        <input name="isQuestion" type="checkbox" checked={this.state.nodeToEdit.isQuestion} onChange={this.handleCheckBoxChange} />
                        <textarea defaultValue={this.handleDefaultValue()}
                                  onChange={this.handleTextChange} className='form-control' rows='3' />
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
            </>
        );
    }
}

export default EditModalComponent;