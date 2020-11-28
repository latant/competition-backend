setInterval(() => location.reload(), 3000);

/*

const host = location.host;
const id = location.pathname.split('/')[2];
const recordElements = document.getElementsByClassName("league-standings-table-record");

const refresh = () => {
    fetch(`http://${host}/competitions/${id}`)
        .then(resp => resp.json())
        .then(data => {
            console.log(data);
            return data;
        })
        .then(league => league.standingsTable.records.forEach((r, i) => {
            recordElements[i].children[0].innerHTML = r.place;
            recordElements[i].children[1].innerHTML = r.competitorName;
            recordElements[i].children[2].innerHTML = r.wins;
            recordElements[i].children[3].innerHTML = r.scores;
            recordElements[i].children[4].innerHTML = `${r.matchesPlayed} / ${r.matchesCount}`;
        }))
        .catch(err => console.log(err))
}

refresh();
setInterval(refresh, 1000);
*/