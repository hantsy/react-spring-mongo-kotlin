import {
  RouteObject,
  RouterProvider,
  createBrowserRouter,
} from "react-router-dom";
import "./App.css";
import { ContactsLayout, PersonEdit, PersonList } from "./contacts";
import { Home, HomeLayout} from "./home";
import ErrorPage from "./ErrorPage";

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
      { path: "", element: <PersonList /> },
      { path: ":id", element: <PersonEdit /> },
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
