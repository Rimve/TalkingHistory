import React from 'react';
import EditModalComponent from "./EditModalComponent";
import cytoscape from 'cytoscape';
import cxtmenu from 'cytoscape-cxtmenu';
import {withRouter} from "react-router-dom";
import {
    getTargetNodeOfIndex,
    getNodeOfIdRef,
    getCharAdjRef,
    getCharNodeRef,
    getDstNode
} from "../services/DatabaseService";
import "firebase/database";
import '../styles/CyStyle.css';

class GraphComponent extends React.Component{

    constructor(props){
        super(props);
        this.renderCytoscapeElement = this.renderCytoscapeElement.bind(this);
        this.state = {
            nodes: [],
            edges: [],
            nodeToEdit: null,
            name: "",
            update: false,
            showEdit: false
        };
    }

    showCallback = (data) => {
        this.setState({showEdit: data});
    };

    handleEdit = (data) => {
        const {name} = this.state;

        this.addNodeToDatabase(name, data);
        this.setState({update: false});
    };

    renderCytoscapeElement() {

        if (!cytoscape('core', 'cxtmenu')) {
            cytoscape.use(cxtmenu);
        }

        let defaults = {
            menuRadius: function(ele){ return 50; },
            selector: 'node',
            commands: [
                {
                    fillColor: 'rgba(222,4,0,0.6)',
                    opacity: 0.1,
                    content: 'Remove',
                    contentStyle: {},
                    select: (ele) => {
                        this.removeNode(ele.data().id);
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(222,4,0,0.6)',
                    content: 'Edit',
                    contentStyle: {},
                    select: (ele) => {
                        this.setState({nodeToEdit: ele.data()});
                        this.setState({showEdit: true});
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(222,4,0,0.6)',
                    content: 'Add',
                    contentStyle: {},
                    select: (ele) => {
                        this.addNode(ele.data());
                    },
                    enabled: true
                }
            ],
            fillColor: 'rgba(0, 0, 0, 0.75)',
            activeFillColor: 'rgba(59,82,86,0.5)',
            activePadding: 20,
            indicatorSize: 12,
            separatorWidth: 5,
            spotlightPadding: 5,
            adaptativeNodeSpotlightRadius: false,
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
                        'height': 80,
                        'width': 80,
                        'background-color' : '#dbd9ff',
                        'background-fit': 'cover',
                        'border-color': '#7a070c',
                        'border-width': 2,
                        'border-opacity': 0.5,
                        'content': 'data(name)',
                        'text-valign': 'center',
                        'overlay-opacity': 0,
                        'label': 'data(scratch)',
                        'shape': 'ellipse'
                    })
                    .selector('edge')
                    .css({
                        'width': 6,
                        'target-arrow-shape': 'triangle',
                        'line-color': '#de0e00',
                        'target-arrow-color': '#de0e00',
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
                    spacingFactor: 0.75,
                    nodeDimensionsIncludeLabels: true
                }
            });
        cy.cxtmenu(defaults);
    }

    async getData(name) {
        let nodes = [];
        let edges = [];

        await getCharNodeRef(name).once('value')
            .then((snapshot) => {
                let results = Object.entries(snapshot.val());
                for (let index in results) {
                    const node = {
                        "data": {
                            id: Number(results[index][0]),
                            scratch: results[index][1]
                        }
                    };
                    nodes.push(node);
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

        this.setState({nodeToEdit: newNode.data});
        this.setState({showEdit: true});

        const edge = {
            "data": {
                source: nodes[nodes.indexOf(nodeToFind)].data.id,
                target: nodes[nodes.indexOf(newNode)].data.id
            }
        };
        edges.push(edge);

        this.setState({
            nodes: nodes,
            edges: edges,
            update: false
        });

        this.addNodeToDatabase(name, newNode.data);
        this.addAdjToDatabase(name, nodes[nodes.indexOf(nodeToFind)].data, nodes[nodes.indexOf(newNode)].data);
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

    getNodeByIndex(nodes, nodeToFind) {
        return nodes.find((node) => node.data.id === nodeToFind);
    }

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
        return (
            <div className="graph-container" id="cy">
                {this.modalComponent()}
            </div>
        );
    }
}

export default withRouter(GraphComponent);