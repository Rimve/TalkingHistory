import React from 'react';
import EditModalComponent from "./EditModalComponent";
import cytoscape from 'cytoscape';
import cxtmenu from 'cytoscape-cxtmenu';
import {withRouter} from "react-router-dom";
import {
    getTargetNodeOfIndex, getNodeOfIdRef,
    getCharAdjRef, getCharNodeRef,
    getDstNode, getCharQuestionsRef,
    getCharQuestionOfIdRef, getCharAudioStorageRef,
    getCharAudioFileRef, getCurrentUserRole
} from "../services/DatabaseService";
import "firebase/database";
import '../styles/CyStyle.css';
import UploadModalComponent from "./UploadModalComponent";
import PageLoadingComponent from "./PageLoadingComponent";
import {ROLES} from "../data/Roles";
import {Alert, AlertTitle} from "@material-ui/lab";
import AlertMassage from "./AlertMessage";

class GraphComponent extends React.Component {

    constructor(props){
        super(props);
        this.renderCytoscapeElement = this.renderCytoscapeElement.bind(this);
        this.state = {
            nodes: [],
            edges: [],
            questionNodeIds: [],
            nodeToEdit: null,
            nodeToAttachFileTo: null,
            nodeToConnectFrom: null,
            userRole: null,
            name: "",
            update: false,
            showEdit: false,
            showUpload: false,
            showSuccess: false,
            showFailure: false
        };
    }

    showSuccessAlert() {
        return (
            <AlertMassage message={"File uploaded successfully!"}
                          severity={"success"}
                          show={this.state.showSuccess}
                          showAlert={this.showAlertCallback}/>
        )
    }

    showFailureAlert() {
        return (
            <AlertMassage message={"File uploading failed."}
                          severity={"error"}
                          show={this.state.showFailure}
                          showAlert={this.showAlertCallback}/>
        )
    }

    showAlertCallback = (data) => {
        this.setState({showSuccess: data});
        this.setState({showFailure: data});
    };

    showEditCallback = (data) => {
        this.setState({showEdit: data});
    };

    showUploadCallback = (data) => {
        this.setState({showUpload: data});
    };

    handleEdit = (node) => {
        const {name} = this.state;
        const {questionNodeIds} = this.state;

        let questions = questionNodeIds;

        if (node.isQuestion) {
            this.setNodeAsQuestion(name, node);
            questions.push(Number(node.data.id));
        }
        else {
            this.removeNodeAsQuestion(name, node);
            questions = questions.filter((item) => item !== node.data.id)
        }

        this.addNodeToDatabase(name, node.data);
        this.setState({
            update: false,
            questionNodeIds: questions
        });
    };

    handleUpload = (file) => {
        const {name} = this.state;
        const {nodeToAttachFileTo} = this.state;

        if (file !== null) {
            let fileName = file.name.split('.');
            getCharAudioStorageRef(name, file.name).put(file).then((snapshot) => {
                getCharAudioFileRef(name, nodeToAttachFileTo.id).set(fileName[0]);
            });
            this.setState({showSuccess: true})
        } else {
            this.setState({showFailure: true})
        }
    };

    renderCytoscapeElement() {
        const {questionNodeIds} = this.state;

        if (!cytoscape('core', 'cxtmenu')) {
            cytoscape.use(cxtmenu);
        }

        let nodeCtxSettings = {
            menuRadius: function(ele){ return 50; },
            selector: 'node',
            commands: [
                {
                    fillColor: 'rgba(255,237,189,0.6)',
                    opacity: 0.1,
                    content: 'Remove',
                    contentStyle: {},
                    select: (ele) => {
                        this.removeNode(ele.data().id);
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(255,237,189,0.6)',
                    content: 'Edit',
                    contentStyle: {},
                    select: (ele) => {
                        let nodeToEdit = this.getNodeByIndex(this.state.nodes, ele.data().id);
                        this.setState({nodeToEdit: nodeToEdit});
                        this.setState({showEdit: true});
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(255,237,189,0.6)',
                    content: 'Add',
                    contentStyle: {},
                    select: (ele) => {
                        this.addNode(ele.data());
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(255,237,189,0.6)',
                    content: 'Connect to..',
                    contentStyle: {},
                    select: (ele) => {
                        if (this.state.nodeToConnectFrom == null) {
                            this.setState({nodeToConnectFrom: ele.data()});
                        }
                        else {
                            this.setState({nodeToConnectFrom: null});
                        }
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(255,237,189,0.6)',
                    content: true ? 'Upload' : 'Remove file',
                    contentStyle: {},
                    select: (ele) => {
                        this.setState({nodeToAttachFileTo: ele.data()});
                        this.setState({showUpload: true});
                    },
                    enabled: true
                }
            ],
            fillColor: 'rgba(0, 0, 0, 0.75)',
            activeFillColor: 'rgba(59,82,86,0.2)',
            activePadding: 3,
            indicatorSize: 12,
            separatorWidth: 5,
            spotlightPadding: 5,
            adaptativeNodeSpotlightRadius: true,
            minSpotlightRadius: 24,
            maxSpotlightRadius: 38,
            openMenuEvents: 'cxttapstart taphold',
            itemColor: '#8facbf',
            itemTextShadowColor: 'transparent',
            zIndex: 9999,
            atMouse: false,
            outsideMenuCancel: false
        };

        let edgeCtxSettings = {
            menuRadius: function(ele){ return 50; },
            selector: 'edge',
            commands: [
                {
                    fillColor: 'rgba(222,155,39,0.6)',
                    opacity: 0.1,
                    content: 'Remove',
                    contentStyle: {},
                    select: (ele) => {
                        this.removeEdge(ele.data().source, ele.data().target);
                    },
                    enabled: true
                }
            ],
            fillColor: 'rgba(0, 0, 0, 0.75)',
            activeFillColor: 'rgba(59,82,86,0.5)',
            activePadding: 3,
            indicatorSize: 12,
            separatorWidth: 0,
            spotlightPadding: 5,
            adaptativeNodeSpotlightRadius: true,
            minSpotlightRadius: 24,
            maxSpotlightRadius: 38,
            openMenuEvents: 'cxttapstart taphold',
            itemColor: 'white',
            itemTextShadowColor: 'transparent',
            zIndex: 9999,
            atMouse: false,
            outsideMenuCancel: false
        };

        let cy = cytoscape(
            {
                container: document.getElementById('cy'),

                boxSelectionEnabled: false,
                autounselectify: true,
                wheelSensitivity: 0.1,

                style: cytoscape.stylesheet()
                    .selector('node')
                    .css({
                        'height': 'label',
                        'width': 'label',
                        'background-color' : '#8facbf',
                        'background-fit': 'cover',
                        'border-color': '#ffedbd',
                        'border-width': 2,
                        'border-opacity': 0.5,
                        'content': 'data(name)',
                        'text-valign': 'center',
                        'text-wrap': 'wrap',
                        'text-max-width': 275,
                        'overlay-opacity': 0,
                        'label': 'data(scratch)',
                        'shape': 'circle',
                        'padding': 25
                    })
                    .selector('edge')
                    .css({
                        'width': 6,
                        'target-arrow-shape': 'triangle',
                        'line-color': '#8facbf',
                        'target-arrow-color': '#8facbf',
                        'overlay-opacity': 0,
                        'curve-style': 'bezier'
                    })
                ,
                elements: {
                    nodes: this.state.nodes,
                    edges: this.state.edges
                },

                layout: {
                    name: 'breadthfirst',
                    directed: true,
                    padding: 20,
                    avoidOverlap: true,
                    spacingFactor: 1,
                    nodeDimensionsIncludeLabels: true
                }
            });

        if (this.state.userRole !== ROLES.USER) {
            // Add context menu on nodes
            cy.cxtmenu(nodeCtxSettings);

            // Add context menu on edges
            cy.cxtmenu(edgeCtxSettings);
        }

        // Add a way to connect one node to already existing one
        cy.on('click', 'node', (evt) => {
            const {nodeToConnectFrom} = this.state;
            if (nodeToConnectFrom != null) {
                this.connectNodes(evt.target.id(), nodeToConnectFrom.id)
            }
        });

        for (let index in questionNodeIds) {
            cy.getElementById(questionNodeIds[index])
                .style('background-color', '#ffedbd')
                .style('border-color', '#8facbf');
        }
    }

    async getData(name) {
        let nodes = [];
        let edges = [];
        let questionNodes = [];

        await getCurrentUserRole().once("value")
            .then((data) => {
                this.setState({userRole: data.val()})
            })

        await getCharNodeRef(name).once('value')
            .then((snapshot) => {
                let results = Object.entries(snapshot.val());
                for (let index in results) {
                    const node = {
                        "data": {
                            id: Number(results[index][0]),
                            scratch: results[index][1]
                        },
                        "isQuestion": false
                    };
                    nodes.push(node);
                }
            });

        await getCharQuestionsRef(name).once('value')
            .then((snapshot) => {
                if (snapshot.val() != null) {
                    let results = Object.entries(snapshot.val());
                    for (let index in results) {
                        let questionNode = this.getNodeByIndex(nodes, Number(results[index][0]));
                        nodes[nodes.indexOf(questionNode)].isQuestion = true;
                        questionNodes.push(Number(results[index][0]));
                    }
                    this.setState({questionNodeIds: questionNodes});
                }
            });

        await getCharAdjRef(name).once('value')
            .then((snapshot) => {
                let results = snapshot.val();
                for (let srcNodeIndex in results) {
                    let resultObj = Object.values(results[srcNodeIndex]);
                    let srcNode = this.getNodeByIndex(nodes, Number(srcNodeIndex));
                    resultObj.map((data, x) => {
                        let targetNode = this.getNodeByIndex(nodes, Number(data));
                        const edge = {
                            "data": {
                                source: nodes[nodes.indexOf(srcNode)].data.id,
                                target: nodes[nodes.indexOf(targetNode)].data.id
                            }
                        };
                        edges.push(edge);
                    })
                }

                this.setState({
                    edges: edges,
                    nodes: nodes,
                    update: true
                });
            });
    }

    getNodesArraySize(srcNode) {
        const {edges} = this.state;
        return edges.filter(edge => edge.data.source === srcNode.id).length;
    }

    addNode(selectedNode) {
        const {nodes} = this.state;
        const {edges} = this.state;
        const {name} = this.state;

        let lastId = Number(nodes[nodes.length - 1].data.id) + 1;
        let nodeToFind = this.getNodeByIndex(nodes, selectedNode.id);

        const newNode = {
            "data": {
                id: lastId,
                scratch: "empty" + lastId.toString()
            }
        };
        nodes.push(newNode);

        this.setState({nodeToEdit: newNode}, () => {
            this.setState({showEdit: true});
        });

        let fromNodeId = nodes[nodes.indexOf(nodeToFind)].data;
        let toNodeId = nodes[nodes.indexOf(newNode)].data;

        const edge = {
            "data": {
                source: fromNodeId.id,
                target: toNodeId.id
            }
        };
        edges.push(edge);

        this.setState({
            nodes: nodes,
            edges: edges,
            update: false
        });

        this.addNodeToDatabase(name, newNode.data);
        this.addAdjToDatabase(name, fromNodeId, toNodeId);
    }

    removeNode(id) {
        const {nodes} = this.state;
        const {name} = this.state;

        let nodeToDelete = '';
        let filteredNodes = this.state.nodes.filter(function(node) {
            if (node.data.id !== id) {
                return node;
            }
            else {
                nodeToDelete = node;
            }
        });

        let filteredEdges = this.state.edges.filter((edge) => {
            if (edge.data.target === nodeToDelete.data.id ||
                edge.data.source === nodeToDelete.data.id) {
                if (edge.data.target === nodeToDelete.data.id) {
                    nodes.map((node) => {
                        if (node.data.id === edge.data.source) {
                            this.removeTargetAdjFromDb(name, node.data.id, nodeToDelete.data.id);
                        }
                    });
                }
            }
            else {
                return edge;
            }
        });

        this.removeNodeFromDb(name, nodeToDelete);
        this.removeSrcAdjFromDb(name, nodeToDelete);

        this.setState({
            nodes: filteredNodes,
            edges: filteredEdges,
            update: false
        });
    }

    connectNodes(nodeToId, nodeFromId) {
        const {nodes} = this.state;
        const {edges} = this.state;
        const {name} = this.state;

        let fromNodeObj = this.getNodeByIndex(nodes, nodeFromId);
        let toNodeObj = this.getNodeByIndex(nodes, nodeToId);

        let fromNode = nodes[nodes.indexOf(fromNodeObj)].data;
        let toNode = nodes[nodes.indexOf(toNodeObj)].data;

        const edge = {
            "data": {
                source: fromNode.id,
                target: toNode.id
            }
        };
        edges.push(edge);

        this.setState({
            edges: edges,
            nodeToConnectFrom: null,
            update: false
        });

        this.addAdjToDatabase(name, fromNode, toNode);
    }

    removeEdge(srcNodeId, targetNodeId) {
        const {nodes} = this.state;
        const {name} = this.state;

        let filteredEdges = this.state.edges.filter((edge) => {
            if (edge.data.target === targetNodeId && edge.data.source === srcNodeId) {
                nodes.map((node) => {
                    if (node.data.id === edge.data.source) {
                        this.removeTargetAdjFromDb(name, srcNodeId, targetNodeId);
                    }
                });
            }
            else {
                return edge;
            }
        });

        this.setState({
            edges: filteredEdges,
            update: false
        });
    }

    // Adds node entry to database nodes table
    addNodeToDatabase(name, node) {
        getNodeOfIdRef(name, [node.id]).set(node.scratch);
    }

    // Adds adjacency to database of source node to target node
    addAdjToDatabase(name, source, target) {
        let index = this.getNodesArraySize(source);
        getTargetNodeOfIndex(name, source.id, index).set(target.id);
    }

    // Removes the source node from database nodes table
    removeNodeFromDb(name, node) {
        getNodeOfIdRef(name, [node.data.id]).remove();
    }

    // Removes whole array from database adjacencies table of the source node
    removeSrcAdjFromDb(name, node) {
        getDstNode(name, node.data.id).remove();
    }

    // Removes target node from source adjacency
    removeTargetAdjFromDb(name, srcIndex, indexToDelete) {
        getDstNode(name, srcIndex).once('value')
            .then((snapshot) => {
                let results = snapshot.val();
                results.map((data, targetIndex) => {
                    if (Number(data) === Number(indexToDelete)) {
                        getTargetNodeOfIndex(name, Number(srcIndex), Number(targetIndex)).remove();
                    }
                })
            });
    }

    setNodeAsQuestion(name, node) {
        getCharQuestionOfIdRef(name, [node.data.id]).set(node.isQuestion);
    }

    removeNodeAsQuestion(name, node) {
        getCharQuestionOfIdRef(name, [node.data.id]).remove();
    }

    getNodeByIndex(nodes, nodeToFind) {
        return nodes.find((node) => node.data.id === nodeToFind);
    }

    modalComponent() {
        return (
            this.state.showEdit ?
                <EditModalComponent
                    showCallBack={this.showEditCallback}
                    editCallBack={this.handleEdit}
                    show={this.state.showEdit}
                    node={this.state.nodeToEdit}
                /> : null
        )
    }

    uploadComponent() {
        return (
            this.state.showUpload ?
                <UploadModalComponent
                    showUploadCallBack={this.showUploadCallback}
                    uploadCallBack={this.handleUpload}
                    show={this.state.showUpload}
                    type='audio'
                /> : null
        )
    }

    async componentDidMount() {
        if (typeof(this.props.location) !== 'undefined' && this.props.location != null) {
            this.setState({name: this.props.location.state.name});
            await this.getData(this.props.location.state.name);
        }
        else {
            console.log("Fail");
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.update !== this.state.update) {
            this.renderCytoscapeElement();
            this.setState({update: true});
        }
    }

    render() {
        // if (this.state.update) {
            return (
                <div className="graph-container"
                     id="cy"
                     onContextMenu={
                         (e) => {
                             e.preventDefault();
                             e.stopPropagation();
                         }
                     }>
                    {this.modalComponent()}
                    {this.uploadComponent()}
                    {this.state.showSuccess ? this.showSuccessAlert() : null}
                    {this.state.showFailure ? this.showFailureAlert() : null}
                </div>
            )
        // }
        // else return (
        //     <div className='body-loading' id='body'>
        //         <PageLoadingComponent />
        //     </div>
        // )
    }
}

export default withRouter(GraphComponent);