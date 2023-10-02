import { Input } from "reactstrap";

interface SearchBoxProps {
    query:string;
    handleSearch:(e: any)=>void
}

const SearchBox: React.FC<SearchBoxProps> = (props: SearchBoxProps) => {
  return (
    <Input
      id="query"
      name="query"
      value={props.query||""}
      onKeyUp={(e:any) => props.handleSearch(e.target.value)}
    ></Input>
  );
};

export type {SearchBoxProps};
export default SearchBox;