import React, {Component} from "react";
import {getUndefinedWordRef, getWordSimilaritiesRef} from "../services/FirebaseService";
import PageLoadingComponent from "./PageLoadingComponent";
import '../styles/WordsBody.css';
import '../styles/EditModal.css';
import {Link, withRouter} from "react-router-dom";
import {Dialog, DialogActions, DialogTitle} from "@material-ui/core";
import AlertMassage from "./AlertMessage";

class CharUncategorizedListComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            undefinedChars: [],
            charCatToDelete: null,
            message: null,
            loaded: false,
            showConfirm: false,
            showAlert: false
        }
    }

    showDelete = (data) => {
        const {showConfirm} = this.state
        this.setState({
            charCatToDelete: data,
            showConfirm: !showConfirm
        })
    }

    showAlertCallback = (data) => {
        this.setState({showAlert: data});
    }

    deleteCharUncategorizedWords(char) {
        getUndefinedWordRef().child(char).remove()
            .then(() => {
                this.setState({
                    undefinedChars: this.state.undefinedChars.filter(function(ele) {return ele !== char}),
                    message: "Category of '"+char+"' has been deleted",
                    showAlert: true
                })
            })
    }

    showDeleteConfirm() {
        const {charCatToDelete} = this.state

        if (this.state.showConfirm) {
            return (
                <Dialog open={this.state.showConfirm}
                        TransitionComponent={this.Transition}>
                    <DialogTitle id="alert-dialog-slide-title">Are you sure you want to delete
                        uncategorized words of "{charCatToDelete}" character?</DialogTitle>
                    <DialogActions>
                        <button className='modal-btn cancel-btn' onClick={() => this.showDelete}><b>No</b></button>
                        <button className='modal-btn' onClick={() => this.deleteCharUncategorizedWords(charCatToDelete)}>
                            <b>Yes</b>
                        </button>
                    </DialogActions>
                </Dialog>
            )
        }
    }

    showAlert(message) {
        return (
            <AlertMassage message={message}
                          severity={"success"}
                          show={this.state.showAlert}
                          showAlert={this.showAlertCallback} />
        )
    }

    componentDidMount() {
        let undefinedChars = []

        getUndefinedWordRef().once("value")
            .then((data) => {
                let results = data.val()

                for (let char in results) {
                    undefinedChars.push(char)
                }

                this.setState({
                    undefinedChars: undefinedChars,
                    loaded: true
                })
            })
    }

    render() {
        if (this.state.loaded) {
            return (
                <div className="character-container">
                    <div className="character-list">
                        <ul className="list-group">
                            {
                                this.state.undefinedChars.map((data, index) => {
                                    return (
                                        <li className='category-entry character-item' key={index}>
                                            <Link to={{pathname: '/assign', state: {category: data}}}
                                                  className="fill-width link-no-deco justify-content-between align-items-center">
                                                {data}
                                            </Link>
                                            <span>
                                                <button key={"delete-cat-btn-"+index} onClick={() => this.showDelete(data)}
                                                        className="modal-btn delete-btn">
                                                    Delete
                                                    {this.showDeleteConfirm()}
                                                </button>
                                            </span>
                                        </li>
                                    )
                                })
                            }
                        </ul>
                    </div>
                    {this.state.showAlert ? this.showAlert(this.state.message) : null}
                </div>
            )
        }
        else return (
            <div className='body-loading' id='body'>
                <PageLoadingComponent />
            </div>
        )
    }
}

export default withRouter(CharUncategorizedListComponent)