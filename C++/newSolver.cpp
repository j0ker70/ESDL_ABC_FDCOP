#include <bits/stdc++.h>
using namespace std;

#define dbg(v) cout << __LINE__ << ": " << #v << " = " << v << endl

default_random_engine re(unsigned(time(nullptr)));
uniform_real_distribution<double> unif(0, 1);

int randomInt (int l, int r) { // Return random integer between [l, r]
    return rand() % (r - l + 1) + l;
}

double randomDouble (double fMin, double fMax) { // Return random double between [fMin, fMax]
    double f = unif(re);
    return fMin + f * (fMax - fMin);
}

vector <double> randomDoubleList (double l, double r, int n) {
    vector <double> ret(n);
    for (double &v : ret) {
        v = randomDouble(l, r);
    }
    return ret;
}

struct BinaryFunction {
    int a, b, c, d, e, f; // ax^2 + bx + cy^2 + dy + exy + f
    BinaryFunction (int aa, int bb, int cc, int dd, int ee, int ff): a(aa), b(bb), c(cc), d(dd), e(ee), f(ff) {
    }
    string toString () {
        return to_string(a) + "x^2 + " + to_string(b) + "x + " + to_string(c) + "y^2 + " +
            to_string(d) + "y + " + to_string(e) + "xy + " + to_string(f);
    }
    double value (double x, double y) {
        return a * x * x + b * x + c * y * y + d * y + e * x * y + f;
    }
};

struct Edge {
    int u, v;
    BinaryFunction func;
    Edge (int uu, int vv, BinaryFunction b) : u(uu), v(vv), func(b) {
    }
    double cost (double x, double y) {
        return func.value(x, y);
    }
    string toString() {
        return "From " + to_string(u) + " To " + to_string(v) + " with function -->> " + func.toString();
    }
};

vector <Edge> get_test (int nodes, int test_no) {
    string filename = "tests/d" + to_string(nodes) + "/test_" + to_string(test_no) + ".txt";
    FILE *inputFile = fopen(filename.c_str(), "r");
    assert(inputFile != NULL);
    vector <Edge> ret;
    int u, v, a, b, c;
    while (fscanf(inputFile, "%d %d %d %d %d", &u, &v, &a, &b, &c) != EOF) {
        ret.push_back(Edge(u, v, BinaryFunction(a, 0, c, 0, b, 0)));
    }
    fclose(inputFile);
    return ret;
}

struct Graph {
    int nodes, testNo;
    vector <Edge> edgeList;
    Graph () {}
    Graph (int n, int test) : nodes(n), testNo(test) {
        edgeList = get_test(nodes, testNo);
    }
    double totalCost (const vector <double> &nodeVals) {
        double ret = 0;
        for (Edge ed : edgeList) {
            ret += ed.cost(nodeVals[ed.u], nodeVals[ed.v]);
        }
        return ret;
    }
    string toString() {
        string ret;
        for (Edge ed : edgeList) {
            ret += ed.toString();
            ret += "\n";
        }
        return ret;
    }
};

int lowerBound, upperBound, limit;
Graph graph;

pair <int, int> chooseTwo (int n) {
    assert(n > 2);
    //int p = randomInt(0, (int) graph.edgeList.size() - 1);
    //return {graph.edgeList[p].u, graph.edgeList[p].v};
    int x = randomInt(0, n - 1);
    int y = randomInt(0, n - 2);
    if (y >= x) {
        ++y;
    }
    return {x, y};
}

struct Solution {
    int n, tried;
    double fitness;
    vector <double> pos;
    vector <int> vis;
    Solution () {}
    Solution (int nn) : n(nn) {
        pos = randomDoubleList(lowerBound, upperBound, n);
        vis.resize(n, 0);
        tried = 0;
        fitness = graph.totalCost(pos);
    }
    string toString() {
        string x;
        for (double v : pos) {
            x += to_string(v) + " ";
        }
        return x;
    }
    void setPos (const vector <double> &newPos) {
        pos = newPos;
        fitness = graph.totalCost(pos);
        fill(begin(vis), end(vis), 0);
        tried = 0;
    }
    void fitBounds (double &v) {
        v = max(v, (double) lowerBound);
        v = min(v, (double) upperBound);
    }
    void check () {
        vector <double> now = randomDoubleList(lowerBound, upperBound, n);
        setPos(now);
    }
    bool search (const Solution &Gbest, const Solution &m, const Solution &e, bool flag) {
        Solution newSol = *this;
        auto [j, h] = chooseTwo(n);
        double phi1 = randomDouble(-0.5, 0.5);
        double phi2 = randomDouble(0, 1.0);
        flag = true;
        if (flag) {
            newSol.pos[j] = 0.5 * (m.pos[h] + Gbest.pos[j]) + phi1 * (pos[h] - e.pos[j]) + phi2 * (pos[h] - Gbest.pos[j]);
        }
        else {
            newSol.pos[j] = 0.5 * (m.pos[j] + Gbest.pos[h]) + phi1 * (pos[j] - e.pos[h]) + phi2 * (pos[j] - Gbest.pos[h]);
        }
        fitBounds(newSol.pos[j]);
        newSol.fitness = graph.totalCost(newSol.pos);
        if (newSol.fitness > fitness) {
            setPos(newSol.pos);
            return true;
        }
        ++vis[j];
        if (vis[j] == 1) {
            ++tried;
        }
        if (tried >= limit) {
            check();
            return true;
        }
        return false;
    }
};

struct Population {
    int sn, es, agents;
    vector <double> fit, prob;
    vector <Solution> pop, elite;
    Solution Gbest;
    Population (int SN, int m, int ag) : sn(SN), es(m), agents(ag) {
        fit.resize(sn);
        prob.resize(sn);
        pop.resize(sn);
        for (auto &v : pop) {
            v = Solution(agents);
        }
        elite.resize(es);
        Gbest = pop[0];
        //for (int i = 0; i < es; i++) {
            //elite[i] = pop[i];
            //if (pop[i].fitness > Gbest.fitness) {
                //Gbest = pop[i];
            //}
        //}
        //for (int i = es; i < sn; i++) {
            //updateValues(pop[i]);
        //}
        for (int i = 0; i < sn; i++) {
            updateValues(pop[i]);
        }
    }
    void buildElite () {
        for (int i = 0; i < es; i++) {
            elite[i] = pop[i];
        }
        for (int j = es; j < sn; j++) {
            int mnPos = 0;
            for (int i = 0; i < es; i++) {
                if (elite[mnPos].fitness > elite[i].fitness) {
                    mnPos = i;
                }
            }
            if (elite[mnPos].fitness < pop[j].fitness) {
                elite[mnPos] = pop[j];
            }
        }
    }
    void updateValues (const Solution &s) {
        //int mnPos = 0;
        //for (int i = 1; i < es; i++) {
            //if (elite[mnPos].fitness > elite[i].fitness) {
                //mnPos = i;
            //}
        //}
        //if (elite[mnPos].fitness < s.fitness) {
            //elite[mnPos] = s;
        //}
        if (Gbest.fitness < s.fitness) {
            Gbest = s;
        }
    }
    void build () {
        double sum = 0;
        for (int i = 0; i < sn; i++) {
            if (pop[i].fitness > 0) {
                fit[i] = 1 + pop[i].fitness;
            }
            else {
                fit[i] = 1.0 / (1 - pop[i].fitness);
            }
            assert(fit[i] >= 0);
            sum += fit[i];
        }
        for (int i = 0; i < sn; i++) {
            prob[i] = fit[i] / sum;
        }
    }
    int chooseOne () {
        double x = randomDouble(0, 1);
        double sum = 0;
        for (int i = 0; i < sn; i++) {
            sum += prob[i];
            if (x <= sum) {
                return i;
            }
        }
        return -1;
    }
    void searchSpace (int i, int m = -1, bool flag = true) {
        int e = randomInt(0, es - 1);
        if (m == -1) {
            m = e;
        }
        if (pop[i].search(Gbest, elite[m], elite[e], flag)) {
            updateValues(pop[i]);
        }
    }
    void employed () {
        for (int i = 0; i < sn; i++) {
            searchSpace(i);
        }
    }
    void onlooker () {
        build();
        for (int i = 0; i < 2 * sn; i++) {
            int j = chooseOne();
            assert(j >= 0);
            for (int k = 0; k < es; k++) {
                searchSpace(j, k, false);
            }
            //searchSpace(j, -1, false);
        }
    }
    double solve (int iterations) {
        //double hun = 0;
        for (int itr = 0; itr < iterations; itr++) {
            buildElite();
            employed();
            onlooker();
        }
        return Gbest.fitness;
    }
};

void initialize () {
    srand((unsigned) time(NULL));
    lowerBound = -50;
    upperBound = 50;
}

double singleSimulation (int populationSize, int eliteSize, int ag) {
    Population population(populationSize, eliteSize, ag);
    double val = population.solve(200);
    return val;
}

double singleAgent (int ag, int probs, int sim, int popSize, int m) {
    double bsum = 0;
    limit = ag;
    int populationSize = popSize, eliteSize = m;
    for (int prb = 0; prb < probs; prb++) {
        graph = Graph(ag, prb);
        double sum = 0;
        //clock_t prStart = clock();
        for (int ss = 0; ss < sim; ss++) {
            //clock_t start = clock();
            double x = singleSimulation(populationSize, eliteSize, ag);
            sum += x;
            //clock_t end = clock();
            //double secs = ((double) end - (double) start) / CLOCKS_PER_SEC;
            //printf("Agents = %d Test = %d Simulation = %d took = %f seconds with %f utility\n", ag, prb, ss, secs, x);
        }
        //clock_t prEnd = clock();
        //double prTime = ((double) prEnd - (double) prStart) / CLOCKS_PER_SEC;
        //printf("Agent %d for Problem %d took %f seconds\n", ag, prb, prTime);
        sum /= sim;
        //printf("Prb = %d util = %f\n", prb, sum);
        bsum += sum;
    }
    bsum /= probs;
    return bsum;
}

void getSolution (int l, int r, int d, int popSize, int m) {
    int probs = 1, sim = 30;
    vector <double> ans;
    vector <int> ind;
    for (int ag = l; ag <= r; ag += d) {
        //clock_t agStart = clock();
        double bsum = singleAgent(ag, probs, sim, popSize, m);
        //clock_t agEnd = clock();
        ind.push_back(ag);
        ans.push_back(bsum);
        //double agTime = ((double) agEnd - (double) agStart) / CLOCKS_PER_SEC;
        //printf("Totally Agent %d took %f seconds\n", ag, agTime);
    }
    //vector <double> acd = {61642.647144320574, 190335.84497646868, 454359.54315174354, 619991.6199635535, 1195038.142493013, 1131920.5143238008, 1508185.2846762734, 1813618.7907109975, 1967699.401151102, 2400034.2480084863, 2332241.906279443, 2632224.59314475, 3140083.2412446854, 2757906.0987709262, 3151800.9298821674, 3216770.3639862314, 3485200.112009816, 3881631.778395952, 4004696.401648546, 4094708.083591433};
    //vector <double> pfd = {61642.89, 190347.523333333, 441271.576666667, 551545.813333333, 1092976.49, 972944.963333333, 1243297.643333333, 1613352.146666666, 1723572.776666667, 1933466.51, 1855290.26, 2102850.12, 2633222.293333334, 2324320.65, 2188000.42, 2395287.276666667, 2817707.206666667, 3086144.89, 3319770.203333333, 3155351.386666667};
    //for (int i = 0; i < (int) ans.size(); i++) {
        //printf("Agents %d Utility = %f diff = %f progress = %f\n", (i + 1) * 5, ans[i], ((ans[i] - acd[i]) * 100) / acd[i], ((ans[i] - pfd[i]) * 100) / pfd[i]);
    //}
    for (int i = 0; i < (int) ans.size(); i++) {
        printf("%d %d %f\n", m, ind[i], ans[i]);
    }
}

void testing () {
    int x = 100;
    graph = Graph(x, 0);
    double ans = singleSimulation(100, 5, x);
    printf("Answer = %lf\n", ans);
}

int main() {
    //clock_t tst = clock();
    initialize();
    //for (int pop : {20, 50, 100, 200, 300}) {
        //getSolution(40, 100, 20, pop, 5);
    //}
    //for (int m = 5; m <= 25; m += 5) {
        //getSolution(40, 100, 20, 100, m);
    //}
    //clock_t ten = clock();
    //double ttm = ((double) ten - (double) tst) / CLOCKS_PER_SEC;
    //printf("Total time took %d minute and %f seconds\n", (int) ttm / 60, ttm - ((int) ttm / 60) * 60);
    //double ans = singleAgent(25, 1, 1);
    //dbg(ans);
    
    testing();
    return 0;
}

