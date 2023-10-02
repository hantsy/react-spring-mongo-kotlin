import { Link } from "react-router-dom";
import { Button, Container } from "reactstrap";

const HomePage = () => {
  return (
    <div>
      <Container fluid>
        <Button color="link">
          <Link to="/contacts">Contact List</Link>
        </Button>
      </Container>
    </div>
  );
};

export default HomePage;
