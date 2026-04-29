import { useEffect, useState } from "react";
import API from "../services/api";

function LossList() {
  const [losses, setLosses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchLosses();
  }, []);

  const fetchLosses = async () => {
    try {
      const res = await API.get("/losses");
      setLosses(res.data);
    } catch (err) {
      console.error(err);
      setError("Failed to fetch data");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h1>Loss List</h1>

      {/* Loading state */}
      {loading && <p>Loading...</p>}

      {/* Error state */}
      {error && <p style={{ color: "red" }}>{error}</p>}

      {/* Data table */}
      {!loading && !error && (
        <table border="1" cellPadding="10" style={{ marginTop: "10px" }}>
          <thead>
            <tr>
              <th>ID</th>
              <th>Amount</th>
              <th>Description</th>
            </tr>
          </thead>
          <tbody>
            {losses.length > 0 ? (
              losses.map((loss) => (
                <tr key={loss.id}>
                  <td>{loss.id}</td>
                  <td>{loss.amount}</td>
                  <td>{loss.description}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="3">No data available</td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default LossList;