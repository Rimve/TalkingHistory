import React, {Component} from "react";
import {getUndefinedWordRef} from "../services/DatabaseService";
import PageLoadingComponent from "./PageLoadingComponent";
import '../styles/WordsBody.css';
import '../styles/EditModal.css';
import {Link, withRouter} from "react-router-dom";

class CharUncategorizedListComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            undefinedChars: [],
            loaded: false
        }
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
                                                <button key={"delete-cat-btn-"+index} onClick={() => this.deleteCategory(data)}
                                                        className="modal-btn delete-btn">
                                                    Delete
                                                </button>
                                            </span>
                                        </li>
                                    )
                                })
                            }
                        </ul>
                    </div>
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