import { createContext, useContext } from 'react';

// https://medium.com/the-guild/injectable-services-in-react-de0136b6d476
const personsBaseUrl = '/api/persons';

const ServiceContext = createContext(null);

const ServiceProvider = (props: any) => {
  const value = {
    getAll: props.getAll || getAll,
    getById: props.getById || getById,
    save: props.save || save,
    update: props.update || update,
    deleteById: props.deleteById || deleteById,
  } as any;

  return (
    <ServiceContext.Provider value={value}>
      ...props.children
    </ServiceContext.Provider>
  );
};

const useService = () => {
  return useContext(ServiceContext);
};

const getAll = (
  query: string | null,
  offset: number = 0,
  limit: number = 10
) => {
  const params = new URLSearchParams();
  if (query) {
    params.append('q', query);
  }
  params.append('offset', offset + '');
  params.append('limit', limit + '');
  return fetch(`${personsBaseUrl}`, {
    body: params,
    headers: {
      Accept: 'application/json',
    },
  });
};

const getById = (id: string) => {
  return fetch(`${personsBaseUrl}/${id}`, {
    headers: {
      Accept: 'application/json',
    },
  });
};

const save = (body: any) => {
  return fetch(`${personsBaseUrl}`, {
    body: JSON.stringify(body),
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

const update = (id: string, body: any) => {
  return fetch(`${personsBaseUrl}/${id}`, {
    body: JSON.stringify(body),
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
  });
};

const deleteById = (id: string) => {
  return fetch(`${personsBaseUrl}/${id}`, {
    method: 'DELETE',
  });
};

export { ServiceProvider, useService };
