interface Person {
  id: string;
  firstName: string;
  lastName: string;
  birthOfDate: Date | undefined;
  email: string | undefined;
  phoneNumber: string | undefined;
  address: Address | undefined;
}

interface PersonSummary {
  id: string;
  name: string;
  birthOfDate: Date | undefined;
  email: string | undefined;
}
interface Address {
  line1: string;
  line2: string | undefined;
  street: string | undefined;
  city: string | undefined;
  zipCode: string | undefined;
}

export type { Person, PersonSummary, Address};
