import logo from './logo.svg';
import './App.css';


const onNaverLogin = () => {
  window.location.href = "http://localhost:8080/oauth2/authorization/naver"
}

const getData = () => {
  fetch("http://localhost:8080/my",
    {
      method: "GET",
      credentials: 'include'
    })
    .then((res) => res.text())
    .then((data) => {
      alert(data);
    })
    .catch((error) => alert(error));
}

function App() {
  return (
    <div className="App">
      <button onClick={onNaverLogin}>NAVER LOGIN</button>
      <button onClick={getData}>GET DATA</button>
    </div>
  );
}

export default App;
