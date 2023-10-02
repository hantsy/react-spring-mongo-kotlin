class PaginatedResult<T> {
  public constructor(
     readonly data: T[],
     readonly count: number
  ) {}
}

export {PaginatedResult};
