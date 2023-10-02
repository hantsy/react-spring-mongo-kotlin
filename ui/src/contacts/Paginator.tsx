import { Link } from "react-router-dom";
import { Pagination, PaginationItem, PaginationLink } from "reactstrap";

interface PaginatorProps {
  offset: number;
  limit: number;
  count: number;
  handlePageClick: (e: any) => void;
}

const Paginator: React.FC<PaginatorProps> = (props: PaginatorProps) => {
  const page = props.offset / props.limit;
  const hasPreviousPage = page > 0;
  const totalPageCount = Math.ceil(props.count / props.limit);
  const hasNextPage = page < totalPageCount - 1;
  const pages: number[] = [-4, -3, -2, -1, 0, 1, 2, 3, 4]
    .map((it) => it + page)
    .filter((it) => it >= 0 && it < totalPageCount);

  console.log("show pages:" + pages);

  const showPreviousPage = () => {
    if (hasPreviousPage) {
      return (
        <PaginationItem>
          <PaginationLink
            previous
            tag={Link}
            onClick={(e) =>props.handlePageClick((page-1)*props.limit)}
          />
        </PaginationItem>
      );
    }
    return <></>;
  };

  const showNextPage = () => {
    if (hasNextPage) {
      return (
        <PaginationItem>
          <PaginationLink
            tag={Link}
            onClick={(e) =>props.handlePageClick((page+1) * props.limit)}
          />
        </PaginationItem>
      );
    }
    return <></>;
  };

  const showPageItems = pages.map((p) => {
    return (
      <PaginationItem key={p}>
        <PaginationLink
          className={p === page ? "active" : ""}
          tag={Link}
          onClick={(e) =>props.handlePageClick(p*props.limit)}
        >
          p+1
        </PaginationLink>
      </PaginationItem>
    );
  });

  return (
    <Pagination>
      showPreviousPage
      {showPageItems}
      showNextPage
    </Pagination>
  );
};

export { Paginator };
export type { PaginatorProps };
