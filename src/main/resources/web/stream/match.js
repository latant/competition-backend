setInterval(() => location.reload(), 3000);

/*
const host = location.host;
const id = location.pathname.split('/')[2];
const participantElements = document.getElementsByClassName("match-participant");

const refresh = () => {
    fetch(`http://${host}/matches/${id}`)
        .then(resp => resp.json())
        .then(data => {
            console.log(data);
            return data;
        })
        .then(match => match.participants.forEach((p, i) => {
            participantElements[i].children[0].innerHTML = p.competitorName;
            const scoreText = p.score !== null ? p.score : '-';
            participantElements[i].children[1].innerHTML = scoreText;
        }))
        .catch(err => console.log(err))
}

refresh();
setInterval(refresh, 1000);
*/