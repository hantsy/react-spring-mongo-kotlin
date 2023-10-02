type Email = string;
type PhoneNumber = string;

interface Person {
  id: string | null;
  firstName: string | null;
  lastName: string | null;
  birthOfDate: Date | null;
  email: Email | null;
  phoneNumber: PhoneNumber | null;
  address: Address | null;
}

interface PersonSummary {
  id: string | null;
  name: string | null;
  birthOfDate: Date | null;
  email: string | null;
}

interface Address {
  line1: string | null;
  line2: string | null;
  street: string | null;
  city: string | null;
  zipCode: string | null;
}

export type { Person, PersonSummary, Address };
