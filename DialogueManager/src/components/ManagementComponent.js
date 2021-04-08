import React from 'react'
import {getCharDescriptionRef, getCurrentUserRole, getNodesRef} from "../services/DatabaseService";
import '../styles/CharacterSelection.css';
import * as FiIcons from 'react-icons/fi';
import {Link, withRouter} from "react-router-dom";
import ComponentLoadingComponent from "./ComponentLoadingComponent";
import {getCharacterPicture} from "../services/Utilities";
import EditCharModal from "./EditCharModal";
import PageLoadingComponent from "./PageLoadingComponent";
import {ROLES} from "../data/Roles";

class ManagementComponent extends React.Component{
    constructor(props){
        super(props);

        this.state = {
            charList: [],
            loaded: false,
            showEdit: false,
            userRole: null,
            name: '',
            description: ''
        };
    }

    showCallback = (data) => {
        this.setState({showEdit: data, loaded: false});
    };

    async getCharData() {
        getNodesRef().once('value')
            .then((charNodes) => {
                let results = charNodes.val();
                this.buildCharData(results);
            }).catch((e) => {
                console.log(e)
            });
        await getCurrentUserRole().once("value")
            .then((data) => {
                this.setState({userRole: data.val()})
            })
    }

    async buildCharData(results) {
        let chars = [];
        for(let entry in results) {
            await getCharDescriptionRef(entry).once('value')
                .then((description) => {
                    let desc = description.val();
                    if (desc !== null) {
                        const char = {
                            "name" : entry,
                            "size" : Object.values(results[entry]).length,
                            "desc" : desc
                        };
                        chars.push(char);
                    }
                    else {
                        const char = {
                            "name" : entry,
                            "size" : Object.values(results[entry]).length,
                            "desc" : ""
                        };
                        chars.push(char);
                    }
                })
                .catch((e) => {
                    console.log(e)
                });
        }
        this.setState({
            charList: chars,
            loaded: true
        });
    }

    showCharacterCreationBtn() {
        if (this.state.userRole !== ROLES.USER) {
            return (
                <div className="character-entry">
                    <button onClick={() => this.createCharacter()}
                            className="character-item d-flex justify-content-center align-items-center">
                        <b>+</b>
                    </button>
                </div>
            )
        }
    }

    createCharacter() {
        this.setState({name: null, description: null, showEdit: true})
    }

    showModalComponent() {
        return (
            this.state.showEdit ?
                <EditCharModal
                    showCallBack={this.showCallback}
                    description={this.state.description}
                    show={this.state.showEdit}
                    name={this.state.name}
                /> : null
        )
    }

    async componentDidMount() {
        await this.getCharData()
    }

    async componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.state.loaded !== prevState.loaded) {
            await this.getCharData()
        }
    }

    render() {
        if (this.state.loaded) {
            const {charList} = this.state;
            return (
                <div className="character-grid">
                    {this.showModalComponent()}
                    {
                        charList.map((data, index) => {
                            return (
                                <div className="character-entry container-shadow" key={'entry-'+data.name+index}>
                                    <div className="character-banner align-items-center">
                                        <div className='character-text justify-center'>
                                            {data.name}
                                            <label className="character-counter badge item-badge badge-pill">
                                                {data.size}
                                            </label>
                                        </div>
                                        <div>
                                            <button className='button d-flex' id={'button-'+data.name}
                                                    onClick={() =>
                                                        this.setState({
                                                            name: data.name,
                                                            description: data.desc,
                                                            showEdit: true
                                                        })
                                                    }>
                                                <FiIcons.FiEdit />
                                            </button>
                                        </div>
                                    </div>
                                    <Link to={{pathname: '/graph', state: {name: data.name}}} key={'graph-'+data.name+index}
                                          className="text-decoration-none">
                                        <div className='character-picture' id={'character-container-'+data.name}>
                                            <div className='character-picture' id={'loader-'+data.name}>
                                                <ComponentLoadingComponent />
                                            </div>
                                            {getCharacterPicture(data.name,'character-container-'+data.name,
                                                'loader-'+data.name, true)}
                                        </div>
                                    </Link>
                                    <div className="character-text align-items-center">
                                        {data.desc}
                                    </div>
                                </div>
                            )
                        })
                    }
                    {this.showCharacterCreationBtn()}
                </div>
            );
        }
        else return (
            <div className='body-loading' id='body'>
                <PageLoadingComponent />
            </div>
        )
    }
}

export default withRouter(ManagementComponent);