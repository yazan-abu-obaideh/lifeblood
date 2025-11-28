import React from "react";
import { render, screen } from "@testing-library/react-native";
import App from "../App";

describe("App Start", () => {
  it("should display Login text when app starts", () => {
    render(<App />);
    expect(screen.getByText("Login")).toBeTruthy();
  });
});
