import {Component} from "react";
import {getRolesRef, getUserRoleRef} from "../services/FirebaseService";
import {ROLES} from "../data/Roles";

export default class UsersComponent extends Component {
    constructor(props) {
        super(props);

        this.state = {
            users: []
        }
    }

    handleUserRoleChange(uid, role) {
        getUserRoleRef(uid).child("role").set(role)
            .catch((error) => {
                console.log(error.message)
            })
    }

    showSelect(defaultValue, uid) {
        return (
            <select defaultValue={defaultValue}
                    className="form-select"
                    aria-label="Role selection list"
                    onChange={event => this.handleUserRoleChange(uid, event.target.value)}>
                {Object.values(ROLES).map((value, index) => {
                    return (
                        <option value={value} key={index}>
                            {value.toUpperCase()}
                        </option>
                    )
                })}
            </select>
        )
    }

    componentDidMount() {
        let users = []

        getRolesRef().once("value")
            .then((data) => {
                let results = data.val();
                for (let entry in results) {
                    const user = {
                        "uid": entry,
                        "data": results[entry]
                    };
                    users.push(user)
                }
                this.setState({users: users})
            })
    }

    render() {
        return (
            <>
                <table className="table table-hover">
                    <thead className="thead-light">
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col">Email</th>
                        <th scope="col">Role</th>
                    </tr>
                    </thead>
                    <tbody>
                    {this.state.users.map((value, index) => {
                        return (
                            <tr key={index}>
                                <th scope="row">{index}</th>
                                <td>{value.data.email}</td>
                                <td>{this.showSelect(value.data.role, value.uid)}</td>
                            </tr>
                        )
                    })}
                    </tbody>
                </table>
            </>
        )
    }
}