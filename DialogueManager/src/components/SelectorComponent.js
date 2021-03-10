import React from 'react'
import {getNodeOfIdRef, getNodesRef} from "../services/DatabaseService";
import '../styles/CharacterSelection.css';
import {Link, withRouter} from "react-router-dom";
import EditModalComponent from "./EditModalComponent";

class SelectorComponent extends React.Component{
    constructor(props){
        super(props);

        this.state = {
            charList: [],
            loaded: false,
            showEdit: false,
            nodeToEdit: {
                "scratch": "Enter the name of the character you want to create"
            }
        };
    }

    showCallback = (data) => {
        this.setState({showEdit: data});
    };

    handleEdit = (data) => {
        getNodeOfIdRef(data.scratch, 0).set("Node start")
        this.setState({
            nodeToEdit: {
                "scratch": "Enter the name of the character you want to create"
            }
        })
    };

    modalComponent() {
        return (
            this.state.showEdit ?
                <EditModalComponent
                    showCallBack={this.showCallback}
                    editCallBack={this.handleEdit}
                    show={this.state.showEdit}
                    node={this.state.nodeToEdit}
                /> : null
        )
    }

    componentDidMount() {
        getNodesRef().on('value', (snapshot) => {
            let names = [];
            let results = snapshot.val();
            for(let index in results){
                const name = {
                    "name" : index,
                    "size" : Object.values(results[index]).length
                };
                names.push(name);
            }
            this.setState({
                charList: names,
                loaded: true
            });
        });
    }

    render() {
        if (this.state.loaded) {
            const {charList} = this.state;
            return (
                <div className="character-container">
                    <div className="character-list">
                        <ul className="list-group">
                            {
                                charList.map((data, index) => {
                                    return (
                                        <Link to={{pathname: '/graph', state: {name: data.name}}} key={index}
                                              className="character-item d-flex justify-content-between align-items-center">
                                            {data.name}
                                            <span className="badge item-badge badge-pill">{data.size}</span>
                                        </Link>
                                    )
                                })
                            }
                            <button onClick={() => this.setState({showEdit: true})}
                                  className="character-item d-flex justify-content-center align-items-center">
                                <b>+</b>
                            </button>
                        </ul>
                        {this.modalComponent()}
                    </div>
                </div>
            );
        }
        else return null;
    }
}

export default withRouter(SelectorComponent);