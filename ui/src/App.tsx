
import React from 'react';
import './App.css';
import Home from './Home';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import PersonList from './PersonList';
import PersonEdit from './PersonEdit';

const App = () => {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Home/>}/>
        <Route path="/persons" element={<PersonList/>}/>
        <Route path="/persons/:id" element={<PersonEdit/>}/>
      </Routes>
    </Router>
  )
}

export default App;