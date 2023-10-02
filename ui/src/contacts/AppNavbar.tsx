import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Collapse, Nav, Navbar, NavbarBrand, NavbarToggler, NavItem, NavLink } from 'reactstrap';

const AppNavbar = () => {

  const [isOpen, setIsOpen] = useState(false);

  return (
    <Navbar color="dark" dark expand="md" className='mb-3'>
      <NavbarBrand tag={Link} to="/">Address Book</NavbarBrand>
      <NavbarToggler onClick={() => { setIsOpen(!isOpen) }}/>
      <Collapse isOpen={isOpen} navbar>
        <Nav className="justify-content-end" style={{width: "100%"}} navbar>
          <NavItem>
            <NavLink href="https://twitter.com/hantsy">@hantsy</NavLink>
          </NavItem>
          <NavItem>
            <NavLink href="https://github.com/hantsy/react-spring-mongo-kotlin">GitHub</NavLink>
          </NavItem>
        </Nav>
      </Collapse>
    </Navbar>
  );
};

export default AppNavbar;