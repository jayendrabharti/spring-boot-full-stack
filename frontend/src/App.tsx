import { useEffect, useState } from "react";
import "./App.css";
import { SampleDataControllerApi } from "./generated";

function App() {
  const [sampleData, setSampleData] = useState<any>("");
  const sampleDataController = new SampleDataControllerApi();

  useEffect(() => {
    sampleDataController
      .getSampleUser()
      .then((data) => {
        setSampleData(data);
      })
      .catch((error) => {
        console.error("Error fetching sample data:", error);
      });
  },[]);

  return (
    <div>
      <pre>{JSON.stringify(sampleData, null, 2)}</pre>
    </div>
  );
}

export default App;
