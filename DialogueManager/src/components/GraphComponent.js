import React from 'react';
import cytoscape from 'cytoscape';
import cxtmenu from 'cytoscape-cxtmenu';
import '../styles/CyStyle.css';
import {withRouter} from "react-router-dom";
import {dbUpdateAdjacencies, dbUpdateNodes, getCharAdjRef, getCharNodeRef} from "../services/DatabaseService";
import "firebase/database";

class GraphComponent extends React.Component{

    constructor(props){
        super(props);
        this.renderCytoscapeElement = this.renderCytoscapeElement.bind(this);
        this.state = {
            nodes: [],
            edges: [],
            name: "",
            update: false
        };
    }

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
                        this.removeNode(ele.data().scratch);
                    },
                    enabled: true
                },
                {
                    fillColor: 'rgba(222,4,0,0.6)',
                    content: 'Edit',
                    contentStyle: {},
                    select: function(ele) {
                        console.log(ele.data())
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
                        'label': 'data(id)',
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
                    name: 'circle',
                    directed: true,
                    padding: 20,
                    avoidOverlap: true,
                    spacingFactor: 0.75,
                    nodeDimensionsIncludeLabels: true
                }
            });
        cy.cxtmenu(defaults);
    }

    getData(name) {
        let nodes = [];
        let edges = [];

        getCharNodeRef(name).on('value', (snapshot) => {
            let results = snapshot.val();
            for (let index in results) {
                const node = {
                    "data": {
                        id: results[index],
                        scratch: results.indexOf(results[index])
                    }
                };
                nodes.push(node);
            }
        });

        getCharAdjRef(name).on('value', (snapshot) => {
            let results = snapshot.val();
            for (let index in results) {
                let resultObj = Object.values(results[index]);
                resultObj.map((data, x) => {
                    if (typeof (nodes[data]) !== 'undefined') {
                        const edge = {
                            "data": {
                                source: nodes[index].data.id,
                                target: nodes[data].data.id
                            }
                        };
                        edges.push(edge);
                    }
                })

            }

            this.setState({
                edges: edges,
                nodes: nodes,
                update: true
            });
        });
    }

    getIdOfLastNode() {
        const {nodes} = this.state;
        return nodes[nodes.length - 1].data.scratch
    }

    getNodesArraySize(srcNode) {
        const {edges} = this.state;
        return edges.filter(edge => edge.data.source === srcNode.id).length - 1;
    }

    addNode(selectedNode) {
        const {nodes} = this.state;
        const {edges} = this.state;
        const {name} = this.state;

        let lastId = this.getIdOfLastNode() + 1;
        const node = {
            "data": {
                id: "empty" + lastId.toString(),
                scratch: lastId
            }
        };
        nodes.push(node);

        const edge = {
            "data": {
                source: nodes[selectedNode.scratch].data.id,
                target: nodes[lastId].data.id
            }
        };
        edges.push(edge);

        this.setState({
            nodes: nodes,
            edges: edges,
            update: false
        });

        this.addNodeToDatabase(name, node);
        this.addAdjToDatabase(name, nodes[selectedNode.scratch].data, nodes[lastId].data);
    }

    removeNode(id) {
        let nodeToDelete = '';
        let nodes = this.state.nodes.filter(function(node) {
            if (node.data.scratch !== id) {
                return node;
            }
            else {
                nodeToDelete = node;
            }
        });

        let edges = this.state.edges.filter(function(edge) {
            if (edge.data.target === nodeToDelete.data.id ||
                edge.data.source === nodeToDelete.data.id) {

            }
            else {
                return edge;
            }
        });

        this.setState({
            nodes: nodes,
            edges: edges,
            update: false
        });
    }

    addNodeToDatabase(name, node) {
        dbUpdateNodes(name, [node.data.scratch]).set(node.data.id);
    }

    addAdjToDatabase(name, source, target) {
        let index = this.getNodesArraySize(source);
        dbUpdateAdjacencies(name, source.scratch, index).set(target.scratch);
    }

    componentDidMount() {
        if (typeof(this.props.location) !== 'undefined' && this.props.location != null) {
            this.setState({name: this.props.location.state.name});
            this.getData(this.props.location.state.name);
        }
        else {
            console.log("Fail");
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.edges !== this.state.edges ||
            prevState.update !== this.state.update) {
            this.renderCytoscapeElement();
            this.setState({update: true});
            // console.log(this.state.nodes);
            // console.log(this.state.edges);
        }
    }

    render(){
        return (
            <div className="graph-container" id="cy"/>
        );
    }
}

export default withRouter(GraphComponent);