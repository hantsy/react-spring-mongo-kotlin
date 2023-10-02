import { Button, ButtonGroup, Table } from "reactstrap";
import { PersonSummary } from "./Model";
import { Link } from "react-router-dom";
import moment from "moment";

interface ContactListProps {
  persons: PersonSummary[];
  setPersons: (data: PersonSummary[]) => void;
}

const ContactListLines = ({ persons, setPersons }: ContactListProps) => {
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

  return persons.map((person) => {
    return (
      <tr key={person.id}>
        <td style={{ whiteSpace: "nowrap" }}>{person.name}</td>
        <td>{person.email}</td>
        <td>{moment(person.birthOfDate).format("YYYY/MM/DD")}</td>
        <td>
          <ButtonGroup>
            <Button
              size="sm"
              color="primary"
              tag={Link}
              to={"/contacts/" + person.id}
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
};

export const ContactList: React.FC<ContactListProps> = ({
  persons,
  setPersons,
}: ContactListProps) => {
  return (
    <Table className="mt-4">
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Birth Of Date</th>
          <th>Actions</th>
        </tr>
      </thead>
      <tbody>{ContactListLines({persons, setPersons})}</tbody>
    </Table>
  );
};
