import { Outlet } from "react-router-dom";
import { Container } from "reactstrap";
import AppNavbar from "./AppNavbar";

const ContactsLayout = () => {
  return (
    <>
      <AppNavbar />
      <Container fluid="md">
        <Outlet />
      </Container>
    </>
  );
};

export default ContactsLayout;
