const host = location.host;
const matchId = location.pathname.split('/')[2];

const participantElements = document.getElementsByClassName("participant");

const run = () => {

    const ws = new WebSocket(`ws://${host}/matches/${matchId}`);

    ws.onopen = () => {
        console.log("Opened connection...")
    }

    ws.onmessage = ({data}) => {
        const match = JSON.parse(data);
        console.log(match);
        match.participants.forEach((p, i) => {
            const element = participantElements[i];
            element.children[0].innerHTML = p.name;
            element.children[1].innerHTML = p.score;
        })
    };

    ws.onclose = (event) => {
        console.log("Closed connection.");
        window.setTimeout(run, 1000);
    };

};

window.onload = run;