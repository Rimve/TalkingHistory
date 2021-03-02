import React from 'react';
import cytoscape from 'cytoscape';
import cxtmenu from 'cytoscape-cxtmenu';
import '../styles/CyStyle.css';
import {withRouter} from "react-router-dom";
import {getCharAdjRef, getCharNodeRef} from "../services/DatabaseService";
import "firebase/database";

class GraphComponent extends React.Component{

    constructor(props){
        super(props);
        this.renderCytoscapeElement = this.renderCytoscapeElement.bind(this);
        this.state = {
            nodes: [],
            edges: [],
            data: [],
            update: false
        };
    }

    renderCytoscapeElement() {

        if (!cytoscape('core', 'cxtmenu')) {
            cytoscape.use(cxtmenu);
        }

        let defaults = {
            menuRadius: function(ele){ return 50; }, // the outer radius (node center to the end of the menu) in pixels. It is added to the rendered size of the node. Can either be a number or function as in the example.
            selector: 'node', // elements matching this Cytoscape.js selector will trigger cxtmenus
            commands: [
                {
                    fillColor: 'rgba(222,4,0,0.5)', // optional: custom background color for item
                    opacity: 0.1,
                    content: 'Remove', // html/text content to be displayed in the menu
                    contentStyle: {}, // css key:value pairs to set the command's css in js if you want
                    select: function(ele){ // a function to execute when the command is selected
                        console.log(ele.id()) // `ele` holds the reference to the active element
                    },
                    enabled: true // whether the command is selectable
                },
                {
                    fillColor: 'rgba(222,4,0,0.5)',
                    content: 'Edit',
                    contentStyle: {},
                    select: function(ele){
                        console.log(ele.id())
                    },
                    enabled: true
                }
            ],
            fillColor: 'rgba(0, 0, 0, 0.75)', // the background colour of the menu
            activeFillColor: 'rgba(59,82,86,0.5)', // the colour used to indicate the selected command
            activePadding: 20, // additional size in pixels for the active command
            indicatorSize: 12, // the size in pixels of the pointer to the active command, will default to the node size if the node size is smaller than the indicator size,
            separatorWidth: 5, // the empty spacing in pixels between successive commands
            spotlightPadding: 5, // extra spacing in pixels between the element and the spotlight
            adaptativeNodeSpotlightRadius: false, // specify whether the spotlight radius should adapt to the node size
            minSpotlightRadius: 24, // the minimum radius in pixels of the spotlight (ignored for the node if adaptativeNodeSpotlightRadius is enabled but still used for the edge & background)
            maxSpotlightRadius: 38, // the maximum radius in pixels of the spotlight (ignored for the node if adaptativeNodeSpotlightRadius is enabled but still used for the edge & background)
            openMenuEvents: 'cxttapstart taphold', // space-separated cytoscape events that will open the menu; only `cxttapstart` and/or `taphold` work here
            itemColor: 'white', // the colour of text in the command's content
            itemTextShadowColor: 'transparent', // the text shadow colour of the command's content
            zIndex: 9999, // the z-index of the ui div
            atMouse: false, // draw menu at mouse position
            outsideMenuCancel: false // if set to a number, this will cancel the command if the pointer is released outside of the spotlight, padded by the number given
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
                elements: this.state.data,

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

    getData(name) {
        let nodes = [];
        getCharNodeRef(name)
            .on('value', (snapshot) => {
                let results = snapshot.val();
                for (let index in results) {
                    const node = {
                        "data": {
                            id: results[index]
                        }
                    };
                    nodes.push(node);
                }
            });

        getCharAdjRef(name)
            .on('value', (snapshot) => {
                let results = snapshot.val();
                for (let index in results) {
                    let srcIndex = results.indexOf(results[index]);
                    results[index].map((data, index) => {
                        const edge = {
                            "data": {
                                source: nodes[srcIndex].data.id,
                                target: nodes[data].data.id
                            }
                        };
                        nodes.push(edge);
                    })
                }
                this.setState({data: nodes});
            });
    }

    componentDidMount() {
        if (typeof(this.props.location) !== 'undefined' && this.props.location != null) {
            this.getData(this.props.location.state.name);
        }
        else {
            console.log("Fail");
        }
    }

    componentDidUpdate(prevProps, prevState, snapshot) {
        if (prevState.data !== this.state.data) {
            this.renderCytoscapeElement();
        }
    }

    render(){
        return (
            <div className="graph-container" id="cy"/>
        );
    }
}

export default withRouter(GraphComponent);