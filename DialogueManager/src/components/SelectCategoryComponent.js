import React, {Component} from "react";
import {getWordSimilaritiesRef} from "../services/FirebaseService";
import PageLoadingComponent from "./PageLoadingComponent";
import {Link, withRouter} from "react-router-dom";
import CreateCategoryModal from "./CreateCategoryModal";
import '../styles/WordsBody.css';
import {Dialog, DialogActions, DialogTitle, Slide} from "@material-ui/core";
import AlertMassage from "./AlertMessage";

class SelectCategoryComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            categories: [],
            loaded: false,
            showModal: false,
            showConfirm: false,
            showAlert: false,
            categoryToDelete: null
        }
    }

    Transition = React.forwardRef(function Transition(props, ref) {
        return <Slide direction="up" ref={ref} {...props} />;
    });

    showAlertCallback = (data) => {
        this.setState({showAlert: data});
    }

    showCallback = (data) => {
        this.setState({showModal: data, loaded: true});
    }

    showDelete = (data) => {
        const {showConfirm} = this.state
        this.setState({
            categoryToDelete: data,
            showConfirm: !showConfirm
        })
    }

    addNewCat = (data) => {
        const {categories} = this.state
        categories.push(data)
        this.setState({categories: categories});
    }

    createCategory() {
        this.setState({showModal: true})
    }

    deleteCategory(category) {
        getWordSimilaritiesRef().child(category).remove()
            .then(() => {
                this.setState({
                    categories: this.state.categories.filter(function(cat) {return cat !== category}),
                    message: "Category '"+category+"' has been deleted",
                    showAlert: true
                })
            })
    }

    showConfirmation() {
        const {categoryToDelete} = this.state

        if (this.state.showConfirm) {
            return (
                <Dialog open={this.state.showConfirm}
                        TransitionComponent={this.Transition}>
                    <DialogTitle id="alert-dialog-slide-title">Are you sure you want to delete category "{categoryToDelete}"?</DialogTitle>
                    <DialogActions>
                        <button className='modal-btn cancel-btn' onClick={() => this.showDelete}><b>No</b></button>
                        <button className='modal-btn' onClick={() => this.deleteCategory(categoryToDelete)}><b>Yes</b></button>
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

    showModal() {
        return (
            this.state.showModal ?
                <CreateCategoryModal
                    showCallBack={this.showCallback}
                    addCatCallBack={this.addNewCat}
                    show={this.state.showModal}
                /> : null
        )
    }

    componentDidMount() {
        const {categories} = this.state

        getWordSimilaritiesRef().once("value")
            .then((data) => {
                let results = data.val()
                for (let entry in results) {
                    categories.push(entry)
                }
                this.setState({
                    categories: categories,
                    loaded: true
                })
            })
    }

    render() {
        const {categories} = this.state

        if (this.state.loaded) {
            return (
                <>
                    <div className="character-container">
                        <div className="character-list">
                            <ul className="list-group">
                                {
                                    categories.map((data, index) => {
                                        return (
                                            <li className='category-entry character-item' key={index}>
                                                <Link to={{pathname: '/editor', state: {category: data}}}
                                                      className="fill-width link-no-deco justify-content-between align-items-center">
                                                    {data}
                                                </Link>
                                                <span>
                                                    <button key={"delete-cat-btn-"+index} onClick={() => this.showDelete(data)}
                                                            className="modal-btn delete-btn">
                                                            Delete
                                                    </button>
                                                </span>
                                            </li>
                                        )
                                    })
                                }
                                <Link to={{pathname: '/uncategorized'}}
                                      className="character-item d-flex justify-content-center align-items-center">
                                    Neatpažinti
                                </Link>
                                <button onClick={() => this.createCategory()}
                                        className="character-item d-flex justify-content-center align-items-center">
                                    <b>+</b>
                                </button>
                            </ul>
                            {this.showConfirmation()}
                        </div>
                        {this.state.showAlert ? this.showAlert(this.state.message) : null}
                    </div>
                    {this.showModal()}
                </>
            );
        }
        else {
            return (
                <div className='body-loading' id='body'>
                    <PageLoadingComponent />
                </div>
            )
        }
    }
}

export default withRouter(SelectCategoryComponent);