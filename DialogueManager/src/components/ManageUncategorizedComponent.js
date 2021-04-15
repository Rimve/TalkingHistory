import React, {Component} from "react";
import {getUndefinedWordRef, getWordSimilaritiesRef} from "../services/DatabaseService";
import PageLoadingComponent from "./PageLoadingComponent";
import '../styles/WordsBody.css';
import '../styles/EditModal.css';
import {withRouter} from "react-router-dom";
import {FormControl, InputLabel, MenuItem, Select} from "@material-ui/core";

class ManageUncategorizedComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            uncategorizedWords: [],
            categories: [],
            newCat: [],
            loaded: false,
            tableName: null
        }
    }

    handleNewCatChange = (event, word, id) => {
        getWordSimilaritiesRef().child(event.target.value).once("value")
            .then((data) => {
                let results = data.val()
                getWordSimilaritiesRef()
                    .child(event.target.value)
                    .child(results.length)
                    .set(word)
                    .then(() => {
                        this.setState({
                            showAlert: true,
                            message: "'"+word+"' has been added to '"+event.target.value+"' category"
                        })
                    })
            })
    }

    showSelect(word, id) {
        if (this.state.categories) {
            return (
                <FormControl className='drop-down-width'>
                    <InputLabel>Assign category</InputLabel>
                    <Select id={id} defaultValue={''} value={''} onChange={(e) =>
                        this.handleNewCatChange(e, word, id)}>
                        {
                            this.state.categories.map((data, index)=> {
                                return (
                                    <MenuItem key={data} value={data}>{data}</MenuItem>
                                )
                            })
                        }
                    </Select>
                </FormControl>
            )
        }
    }

    componentDidMount() {
        let uncategorizedWords = []
        let categories = []

        if (typeof(this.props.location) !== 'undefined' && this.props.location != null
            && typeof(this.props.location.state) !== 'undefined') {
            const tableName = this.props.location.state.category

            getWordSimilaritiesRef().once("value")
                .then((data) => {
                    let results = data.val()
                    for (let entry in results) {
                        categories.push(entry)
                    }
                    this.setState({categories: categories,})
                })

            getUndefinedWordRef().child(tableName).once("value")
                .then((data) => {
                    let results = data.val()

                    for (let words in results) {
                        for (let word in results[words]) {
                            if (!uncategorizedWords.includes(results[words][word])) {
                                uncategorizedWords.push(results[words][word])
                            }
                        }
                    }

                    this.setState({
                        uncategorizedWords: uncategorizedWords,
                        loaded: true,
                        tableName: tableName
                    })
                })
        }
    }

    render() {
        if (this.state.loaded) {
            return (
                <div className="character-container bottom-margin">
                    <div className="character-list">
                        <ul className="list-group">
                            {
                                this.state.uncategorizedWords.map((data, index) => {
                                    return (
                                        <li className='category-entry character-item' key={index}>
                                            {data}
                                            {this.showSelect(data, "dropdown-"+index)}
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

export default withRouter(ManageUncategorizedComponent)