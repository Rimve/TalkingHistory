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

    handleChange = (change) => {
        let newNode = this.state.nodeToEdit;
        newNode.scratch = change.target.value;
        this.setState({
            nodeToEdit: newNode
        })
    };

    render() {
        return (
            <>
                <Modal show={this.state.show} onHide={this.handleClose} centered>
                    <Modal.Header closeButton>
                        <Modal.Title>Dialogue entry</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <textarea defaultValue={this.state.nodeToEdit.scratch}
                                  onChange={this.handleChange} className='form-control' rows='3' />
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

export default EditModalComponent;