import React from "react";
import Snackbar from "@material-ui/core/Snackbar";
import IconButton from "@material-ui/core/IconButton";
import * as RiIcons from 'react-icons/ri';
import {Alert, AlertTitle} from "@material-ui/lab";

class AlertMassage extends React.Component {

    constructor(props){
        super(props);

        this.state = {
            show: this.props.show,
            severity: this.props.severity,
            message: this.props.message
        };
    }

    handleClose = () => {
        this.props.showAlert(false)
        this.setState({show: false})
    }

    handleOpen = () => {

    }

    whichAlert() {
        if (this.state.message !== null && this.state.severity === "success") {
            return (
                <Alert severity="success">
                    <AlertTitle>Success</AlertTitle>
                    {this.state.message}
                </Alert>
            )
        }
        else if (this.state.message !== null && this.state.severity === "error") {
            return (
                <Alert severity="error">
                    <AlertTitle>Error</AlertTitle>
                    {this.state.message}
                </Alert>
            )
        }
        else if (this.state.message !== null && this.state.severity === "warning") {
            return (
                <Alert severity="warning">
                    <AlertTitle>Warning</AlertTitle>
                    {this.state.message}
                </Alert>
            )
        }
        else if (this.state.severity === "success") {
            return (
                <Alert severity="success">
                    <AlertTitle>Success</AlertTitle>
                </Alert>
            )
        }
        else if (this.state.severity === "error") {
            return (
                <Alert severity="error">
                    <AlertTitle>An error has occurred</AlertTitle>
                </Alert>
            )
        }
        else if (this.state.severity === "warning") {
            return (
                <Alert severity="warning">
                    <AlertTitle>Something is not quite right</AlertTitle>
                </Alert>
            )
        }
    }

    render() {
        return (
            <div>
                <Snackbar
                    anchorOrigin={{vertical: "bottom", horizontal: "center"}}
                    open={this.handleOpen}
                    autoHideDuration={3000}
                    onClose={this.handleClose}
                    ContentProps={{"aria-describedby": "message-id"}}
                    action={[
                        <IconButton key="close" onClick={this.handleClose}>
                            <RiIcons.RiCloseLine/>
                        </IconButton>
                    ]}
                >
                    {this.whichAlert()}
                </Snackbar>
            </div>
        );
    }
}

export default AlertMassage