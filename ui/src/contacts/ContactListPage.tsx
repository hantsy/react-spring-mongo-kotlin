import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { Button } from "reactstrap";
import { ContactList } from "./ContactList";
import { ContactProvider, useContactContext } from "./ContactService";
import { PersonSummary } from "./Model";
import { PaginatedResult } from "./PaginatedResult";
import { Paginator } from "./Paginator";
import SearchBox from "./SearchBox";

const ContactListPage = () => {
  const [persons, setPersons] = useState<PersonSummary[]>([]);
  const [count, setCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(false);
  const [searchParams, setSearchParams] = useSearchParams();
  const contactService = useContactContext();

  useEffect(() => {
    setLoading(true);
    contactService
      .getAll(
        searchParams.get("query"),
        parseInt(searchParams.get("offset") ?? "0"),
        parseInt(searchParams.get("limit") ?? "0")
      )
      .then((res:PaginatedResult<PersonSummary>) => {
        setPersons(res.data);
        setCount(res.count)
        setLoading(false);
      });
  }, [contactService, searchParams]);


  const onQueryChange = (query: string) => {
    setSearchParams({
      q: query,
      offset: "0",
      limit: "10",
    });
  };

  const onPageChange = (offset: number) => {
    setSearchParams({ ...searchParams.entries, offset: offset + "" });
  };

  if (loading) {
    return <p>Loading...</p>;
  }
  return (
    <ContactProvider>
      <h2>Contact List</h2>
      <div className="row mt=5">
        <div className="col-md-6 md-12">
          <SearchBox
            query={searchParams.get("q") ?? ""}
            handleSearch={onQueryChange}
          />
        </div>
        <div className="col-md-6 md-12">
          <Button
            className="float-end"
            color="success"
            tag={Link}
            to="/contacts/new"
          >
            Add Contact
          </Button>
        </div>
      </div>
      <ContactList persons={persons} setPersons={setPersons} />
      <Paginator
        offset={parseInt(searchParams.get("offset") ?? "0")}
        limit={parseInt(searchParams.get("limit") ?? "10")}
        count={count}
        handlePageClick={onPageChange}
      />
    </ContactProvider>
  );
};

export default ContactListPage;
