import { createContext, useContext } from "react";

interface UserContextType {
  userUuid: string | undefined;
  getUserToken: () => Promise<string>;
  setUserUuid: (uuid: string) => void;
  clearUserUuid: () => void;
}

export const UserContext = createContext<UserContextType | undefined>(
  undefined
);

export const useUser = () => {
  const context = useContext(UserContext);
  if (!context) {
    throw new Error("useUser must be used within UserProvider");
  }
  return context;
};
