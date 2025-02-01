$('.menu .apis').click(function() {
    $('#manuals-menu').transition('hide');
    $('#mailing-lists-menu').transition('hide');
    $('#apis-menu').transition('swing down');
});

$('.menu .manuals').click(function() {
    $('#apis-menu').transition('hide');
    $('#mailing-lists-menu').transition('hide');
    $('#manuals-menu').transition('swing down');
});

$('.menu .mailing-lists').click(function() {
    $('#manuals-menu').transition('hide');
    $('#apis-menu').transition('hide');
    $('#mailing-lists-menu').transition('swing down');
});

$.get('https://api.github.com/repos/geb/geb')
    .done(function(body) {
        var watchers = body.subscribers_count;
        var stars = body.stargazers_count;
        var forks = body.network_count;
        $('[data-stars]').text(stars);
        $('[data-watchers]').text(watchers);
        $('[data-forks]').text(forks);
    })
    .catch(function(e) {
        console.error('GitHub api request failed. No stats available.')
        $('[data-github-stats] > .statistic:not(.static)').transition('fade')
    })