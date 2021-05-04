import React, {Component} from "react";
import {getWordSimilaritiesRef} from "../services/FirebaseService";
import PageLoadingComponent from "./PageLoadingComponent";
import '../styles/WordsBody.css';
import '../styles/EditModal.css';
import {withRouter} from "react-router-dom";

class CategorizedWordsComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            similarities: null,
            newWord: null,
            updatedWord: null,
            edit: []
        }
    }

    handleNewWordChange = (event) => this.setState({newWord: event.target.value})
    handleUpdatedWordChange = (event) => this.setState({updatedWord: event.target.value})

    handleEditButton(category, index) {
        const {edit} = this.state

        let tmpEdit = edit
        tmpEdit[category][index] = !edit[category][index]
        this.setState({edit: tmpEdit})
    }

    handleSaveButton(category, index, word) {
        const {similarities} = this.state

        getWordSimilaritiesRef().child(category)
            .child(index.toString())
            .set(word)
            .then(() => {
                for (let entry in similarities) {
                    if (similarities[entry].tableName === category) {
                        let tmpSimilarities = similarities
                        tmpSimilarities[entry].values[index] = word
                        this.setState({
                            similarities: tmpSimilarities,
                            updatedWord: null
                        })
                    }
                }
            })
            .catch((error) => {console.log(error.message)})
    }

    deleteWord(category, index) {
        const {similarities} = this.state

        getWordSimilaritiesRef().child(category)
            .child(index.toString())
            .remove()
            .then(() => {
                for (let entry in similarities) {
                    if (similarities[entry].tableName === category) {
                        similarities[entry].values = similarities[entry].values.filter((_, i) => i !== index)
                        this.setState({similarities: similarities})
                    }
                }
            })
            .catch((error) => {console.log(error.message)})
    }

    addWordTo(category, count, word) {
        const {similarities} = this.state

        let index = count++
        getWordSimilaritiesRef().child(category)
            .child(index.toString())
            .set(word)
            .then(() => {
                for (let entry in similarities) {
                    if (similarities[entry].tableName === category) {
                        similarities[entry].values.push(word)
                    }
                }
            })
            .catch((error) => {console.log(error.message)})
        this.setState({similarities: similarities})
    }

    componentDidMount() {
        let similarities = []
        let edit = []

        if (typeof(this.props.location) !== 'undefined' && this.props.location != null) {
            const tableName = this.props.location.state.category

            getWordSimilaritiesRef().child(tableName).once("value")
                .then((data) => {
                    let results = data.val()
                    const table = {
                        "tableName": tableName,
                        "values": results,
                        "count": results.length
                    }

                    similarities.push(table)
                    edit[tableName] = new Array(results.length).fill(false)

                    this.setState({
                        similarities: similarities,
                        edit: edit
                    })
                })
        }
    }

    render() {
        if (this.state.similarities !== null) {
            return (
                <div className="table-responsive table-container">
                    {this.state.similarities.map((data, index) => {
                        return (
                            <div className="word-container" key={index}>
                                <h3>Kategorija - {data.tableName}</h3>
                                <table className="table table-hover">
                                    <thead className="thead-light">
                                    <tr>
                                        <th scope="col">#</th>
                                        <th scope="col">Word</th>
                                        <th scope="col" colSpan="2">Action</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {data.values.map((value, index) => {
                                        return (
                                            <tr key={index}>
                                                <th scope="row">{index}</th>
                                                <td>{this.state.edit[data.tableName][index] ?
                                                    <input type="text"
                                                           className="form-control"
                                                           defaultValue={value}
                                                           onChange={this.handleUpdatedWordChange}/> : value}
                                                </td>
                                                <td>{this.state.edit[data.tableName][index] ?
                                                    <button className="modal-btn"
                                                            onClick={() => {
                                                                this.handleSaveButton(data.tableName, index, this.state.updatedWord)
                                                                this.handleEditButton(data.tableName, index)
                                                            }}>Save</button> :
                                                    <button className="modal-btn"
                                                            onClick={() => {
                                                                this.handleEditButton(data.tableName, index)
                                                            }}>Edit</button>}
                                                </td>
                                                <td>
                                                    <button className="modal-btn cancel-btn"
                                                            onClick={() => {
                                                                this.deleteWord(data.tableName, index)
                                                            }}>Delete</button>
                                                </td>
                                            </tr>
                                        )
                                    })}
                                    <tr>
                                        <td colSpan="2">
                                            <input type="text"
                                                   className="form-control"
                                                   placeholder="Enter new word"
                                                   onChange={this.handleNewWordChange}/>
                                        </td>
                                        <td colSpan="2">
                                            <button className="modal-btn vertical-align-center"
                                                    onClick={() => {
                                                        this.addWordTo(data.tableName, data.count, this.state.newWord)
                                                    }}>Add</button>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        )
                    })}
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

export default withRouter(CategorizedWordsComponent)