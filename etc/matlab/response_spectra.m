%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
% Response spectra web service example
%
% Author: Demi Girot (dgirot@usgs.gov)
%         Peter Powers (pmpowers@usgs.gov)
%
% Created: 09/16/2021
% Updated: 01/12/2022
%
% This script assembles a response spectra web service URL request, makes
% the request, places the result in a struct, and generates a simple plot.
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Create web service URL
clearvars
% The root url for the response spectra web service
urlbase = "https://staging-earthquake.usgs.gov/ws/nshmp/data/gmm/spectra?";

% The ground motion models (GMMs) of interest
%
% For allowed identifiers, see:
% https://earthquake.usgs.gov/nshmp/docs/nshmp-lib/gov/usgs/earthquake/nshmp/gmm/Gmm.html
gmms = ["BJF_97" "CB_03" "CY_14"];

% Ground motion model parameters
Mw = 6.5;
dip = 90;
rake = 0;
width = 14;
rJB = 10;
rRup = 10.012;
rX = 10;
vs30 = 760;
vsInf = true;
zHyp = 7.5;
zTop = 0.5;

url = createUrl( ...
    urlbase, gmms, ...
    Mw, dip, rake, width, rJB, rRup, rX, vs30, vsInf, zHyp, zTop);
%% Call web service

% Open a browser window with the web service URL to show the JSON response
web(url);

% Call the web service and place response in a struct
data = webread(url);

%% Summary of each GMM dataset

means = data.response.means.data;
sigmas = data.response.sigmas.data;

c = newline;

for i=1:length(means)
    gmm = ...
    "GMM:     " + means(i).label + c + ...
    "Periods: " + mat2str(means(i).data.xs') + c + ...
    "Means:   " + mat2str(means(i).data.ys') + c + ...
    "Sigmas:  " + mat2str(sigmas(i).data.ys')
end

%% Make a simple plot of means data with epistemic uncertainty from web 
% service response

figure(1)
cla

plot_handles = [];
legend_labels = {};

% loop means response array
for i = 1:length(means)
    disp(means(i))

    gmm_id = means(i).id;
    gmm_label = means(i).label;
    gmm_xs = means(i).data.xs;
    gmm_ys = means(i).data.ys;
    epi_tree = means(i).tree;

    % Plot the total spectrum
    PH = semilogx(gmm_xs, gmm_ys, 'LineWidth', 2);
    hold on; grid on
    plot_handles = [plot_handles PH];
    legend_labels{end+1} = gmm_id;

    % Plot epistemic spectra, if present
    if ~isempty(epi_tree)
        for j = 1 : length(epi_tree)
            epi_branch = epi_tree(j);
            epi_label = epi_branch.id;
            epi_ys = epi_branch.values;
            PHE = semilogx(gmm_xs, epi_ys,'LineWidth',1,'LineStyle','--','Color',PH.Color);
            plot_handles = [plot_handles PHE];
            legend_labels{end+1} = [gmm_id ' ' epi_label];
        end

    end
end
 
xlabel('Periods (sec)','FontSize',12)
ylabel('Median Ground Motion (g)','FontSize',12)
title('Ground Motion vs Response Spectra (Means)', 'FontSize', 14)
axis([0.001 10 0.005 0.8]);
set(gca, 'FontSize', 12);
set(gca, 'XTick', [0.01 0.1 1 10]);
set(gca, 'XTickLabel', {'0.01','0.1','1','10'});

l = legend(plot_handles, legend_labels, 'Location', 'northwest');
set(l, 'Interpreter', 'none')


%% Build URL function

function url = createUrl( ...
    urlbase, gmms, ...
    Mw, dip, rake, width, rJB, rRup, rX, vs30, vsInf, zHyp, zTop)

    url = urlbase;
    for i = 1:size(gmms, 2)
        if i == 1
            url = url + "gmm=" + gmms(i);
        else
            url = url + "&gmm=" + gmms(i);
        end
    end
    url = url + ...
        "&Mw=" + num2str(Mw) + ...
        "&dip=" + num2str(dip) + ...
        "&rake=" + num2str(rake) + ...
        "&width=" + num2str(width) + ...
        "&rJB=" + num2str(rJB) + ...
        "&rRup=" + num2str(rRup) + ...
        "&rX=" + num2str(rX) + ...
        "&vs30=" + num2str(vs30) + ...
        "&vsInf=" + string(vsInf) + ...
        "&zHyp=" + num2str(zHyp) + ...
        "&zTop=" + num2str(zTop);
end