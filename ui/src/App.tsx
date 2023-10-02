import React from "react";
import {
  RouteObject,
  RouterProvider,
  createBrowserRouter,
} from "react-router-dom";
import "./App.css";
import ErrorPage from "./ErrorPage";
import { ContactEditPage, ContactListPage, ContactsLayout } from "./contacts";
import { Home, HomeLayout } from "./home";

const routes: RouteObject[] = [
  {
    path: "/",
    element: <HomeLayout />,
    children: [{ path: "", element: <Home /> }],
  },
  {
    path: "/contacts",
    element: <ContactsLayout />,
    children: [
      { path: "", element: <ContactListPage /> },
      { path: ":id", element: <ContactEditPage /> },
    ],
  },
  {
    path: "*",
    element: <ErrorPage/>,
  },
];

const router = createBrowserRouter(routes);

const App = () => {
  return <RouterProvider router={router} />;
};

export default App;
