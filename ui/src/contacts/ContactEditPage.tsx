import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { Button, Form, FormGroup, Input, Label } from "reactstrap";

const ContactEditPage = () => {
  type AddressForm = {
    line1: string;
    line2: string;
    street: string;
    city: string;
    zipCode: string;
  };
  type PersonForm = {
    id: string;
    firstName: string;
    lastName: string;
    birthOfDate: string;
    email: string;
    phoneNumber: string;
    address: AddressForm;
  };

  const initialFormState: PersonForm = {
    id: "",
    firstName: "",
    lastName: "",
    email: "",
    birthOfDate: "",
    phoneNumber: "",
    address: {
      line1: "",
      line2: "",
      street: "",
      city: "",
      zipCode: "",
    },
  };
  const [person, setPerson] = useState<PersonForm>(initialFormState);
  const navigate = useNavigate();
  const { id } = useParams();

  useEffect(() => {
    if (id !== "new") {
      fetch(`/persons/${id}`)
        .then((response) => response.json())
        .then((data) => setPerson(data));
    }
  }, [id, setPerson]);

  const handleChange = (event: any) => {
    const { name, value } = event.target;
    console.log(`handling event:${name}->${value}`);
    setPerson({ ...person, [name]: value });
  };

  const handleAddressLine1 = (event: any) => {
    const line1 = {
      address: { ...person.address, line1: event.target.value },
    } as PersonForm;
    setPerson({ ...person, ...line1 });
  };

  const handleAddressLine2 = (event: any) => {
    const line2 = {
      address: { ...person.address, line2: event.target.value },
    } as PersonForm;
    setPerson({ ...person, ...line2 });
  };
  const handleAddressStreet = (event: any) => {
    const street = {
      address: { ...person.address, street: event.target.value },
    } as PersonForm;
    setPerson({ ...person, ...street });
  };

  const handleAddressCity = (event: any) => {
    const city = {
      address: { ...person.address, city: event.target.value },
    } as PersonForm;
    setPerson({ ...person, ...city });
  };

  const handleAddressZipCode = (event: any) => {
    const zipCode = {
      address: { ...person.address, zipCode: event.target.value },
    } as PersonForm;
    setPerson({ ...person, ...zipCode });
  };

  const handleSubmit = async (event: any) => {
    event.preventDefault();

    const url = person.id ? `/persons/${person.id}` : "/persons";
    await fetch(url, {
      method: person.id ? "PUT" : "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
      },
      body: JSON.stringify(person),
    });

    setPerson(initialFormState);
    navigate("/persons");
  };

  const title = <h2>{person.id ? "Edit Contact" : "Add Contact"}</h2>;

  return (
    <>
      <h2>{title}</h2>
      <Form autoComplete="false" onSubmit={handleSubmit}>
        <div className="row mb-1">
          <FormGroup className="col-md-6 col-12">
            <Label for="lastName">First Name</Label>
            <Input
              type="text"
              name="firstName"
              id="firstName"
              required={true}
              value={person.firstName || ""}
              onChange={handleChange}
            />
          </FormGroup>
          <FormGroup className="col-md-6 col-12">
            <Label for="lastName">Last Name</Label>
            <Input
              type="text"
              name="lastName"
              id="lastName"
              required={true}
              value={person.lastName || ""}
              onChange={handleChange}
            />
          </FormGroup>
        </div>
        <div className="row mb-3">
          <FormGroup className="col-md-4 col-12">
            <Label for="birthOfDate">Birth Of Date</Label>
            <Input
              type="date"
              name="birthOfDate"
              id="birthOfDate"
              value={person.birthOfDate || ""}
              onChange={handleChange}
            />
          </FormGroup>
          <FormGroup className="col-md-5 col-12">
            <Label for="email">Email</Label>
            <Input
              type="email"
              name="email"
              id="email"
              value={person.email || ""}
              onChange={handleChange}
            />
          </FormGroup>
          <FormGroup className="col-md-3 col-12">
            <Label for="phoneNumber">PhoneNumber</Label>
            <Input
              type="text"
              name="phoneNumber"
              id="phoneNumber"
              value={person.phoneNumber || ""}
              onChange={handleChange}
            />
          </FormGroup>
        </div>
        <FormGroup>
          <Label for="line1">Address Line1</Label>
          <Input
            type="text"
            name="address.line1"
            id="line1"
            value={person.address.line1}
            onChange={handleAddressLine1}
          />
        </FormGroup>
        <FormGroup>
          <Label for="line2">Address Line2</Label>
          <Input
            type="text"
            name="address.line2"
            id="line2"
            value={person.address.line2 || ""}
            onChange={handleAddressLine2}
          />
        </FormGroup>
        <div className="row mb-3">
          <FormGroup className="col-md-4 col-12">
            <Label for="street">Street</Label>
            <Input
              type="text"
              name="address.street"
              id="street"
              value={person.address.street || ""}
              onChange={handleAddressStreet}
            />
          </FormGroup>
          <FormGroup className="col-md-5 col-12">
            <Label for="city">City</Label>
            <Input
              type="text"
              name="address.city"
              id="city"
              value={person.address.city || ""}
              onChange={handleAddressCity}
            />
          </FormGroup>
          <FormGroup className="col-md-3 col-12">
            <Label for="country">Zip Code</Label>
            <Input
              type="text"
              name="address.zipCode"
              id="zipCode"
              value={person.address.zipCode || ""}
              onChange={handleAddressZipCode}
            />
          </FormGroup>
        </div>
        <FormGroup>
          <Button color="primary" type="submit">
            Save
          </Button>{" "}
          <Button color="secondary" tag={Link} to="/groups">
            Cancel
          </Button>
        </FormGroup>
      </Form>
    </>
  );
};

export default ContactEditPage;
