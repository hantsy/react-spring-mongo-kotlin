import React, { useEffect, useState } from "react";
import { Button, ButtonGroup, Container, Table } from "reactstrap";
import AppNavbar from "./AppNavbar";
import { Link } from "react-router-dom";
import { PersonSummary } from "./Model";
import moment from "moment";

const PersonList = () => {
  const [persons, setPersons] = useState<PersonSummary[]>([]);
  const [loading, setLoading] = useState<boolean>(false);

  useEffect(() => {
    setLoading(true);

    fetch("/persons")
      .then((response) => response.json())
      .then((data) => {
        console.log("fetched person:" + JSON.stringify(data));
        setPersons(data);
        setLoading(false);
      });
  }, []);

  const remove = async (id: string) => {
    await fetch(`/persons/${id}`, {
      method: "DELETE",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    }).then(() => {
      let updatedGroups = [...persons].filter((i) => i.id !== id);
      setPersons(updatedGroups);
    });
  };

  if (loading) {
    return <p>Loading...</p>;
  }

  const contactList = persons.map((person) => {
    return (
      <tr key={person.id}>
        <td style={{ whiteSpace: "nowrap" }}>{person.name}</td>
        <td>{person.email}</td>
        <td>
          {moment(person.birthOfDate).format("YYYY/MM/DD")}
        </td>
        <td>
          <ButtonGroup>
            <Button
              size="sm"
              color="primary"
              tag={Link}
              to={"/persons/" + person.id}
            >
              Edit
            </Button>
            <Button size="sm" color="danger" onClick={() => remove(person.id)}>
              Delete
            </Button>
          </ButtonGroup>
        </td>
      </tr>
    );
  });

  return (
    <div>
      <AppNavbar />
      <Container fluid>
        <div className="float-end">
          <Button color="success" tag={Link} to="/persons/new">
            Add Contact
          </Button>
        </div>
        <h3>Contact List</h3>
        <Table className="mt-4">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Birth Of Date</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>{contactList}</tbody>
        </Table>
      </Container>
    </div>
  );
};

export default PersonList;
