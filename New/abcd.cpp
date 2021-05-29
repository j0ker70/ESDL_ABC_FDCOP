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
    int uid, vid;
    BinaryFunction func;
    Edge (int uu, int vv, BinaryFunction b) : uid(uu), vid(vv), func(b) {
    }
    double utility (double x, double y) {
        return func.value(x, y);
    }
    string toString () {
        return "From " + to_string(uid) + " To " + to_string(vid) + " with function -->> " + func.toString();
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

struct Agent {
    int id;
    vector <Edge> edges;
    int popSize;
    vector <double> population, localFitness;
    int eliteSize;
    vector <int> elites;
    int limit;
    double upperBound, lowerBound;

    double X;

    Agent(int _id, int _popSize, int _eliteSize, int _limit, double _upperBound, double _lowerBound):
        id(_id), popSize(_popSize), eliteSize(_eliteSize), limit(_limit), upperBound(_upperBound), lowerBound(_lowerBound), X(0) {}

    void addEdge (Edge e) {
        edges.push_back(e);
    }

    void initialize () {
        population = randomDoubleList(lowerBound, upperBound, popSize);
    }

    double localUtils (vector <Agent> &agents) {
        double util = 0;
        for (Edge e : edges) {
            util += e.utility(X, agents[e.vid].X);
        }
        return util;
    }

    double singleUtil (double var, vector <Agent> &agents) {
        double util = 0;
        for (Edge e : edges) {
            util += e.utility(var, agents[e.vid].X);
        }
        return util;
    }

    void calcUtilities (vector <Agent> &agents) {
        localFitness = vector <double> (popSize, 0);
        for (int i = 0; i < popSize; i++) {
            for (Edge e : edges) {
                localFitness[i] += e.utility(population[i], agents[e.v].X);
            }
        }
    }

    void build () {
        elites = vector <int> (eliteSize);
        iota(elites.begin(), elites.end(), 0);
        for (int i = eliteSize; i < popSize; i++) {
            int minPos = 0;
            for (int j = 0; j < eliteSize; j++) {
                if (localFitness[elites[minPos]] > localFitness[elites[j]]) {
                    minPos = j;
                }
            }
            if (localFitness[elites[minPos]] < localFitness[i]) {
                elites[minPos] = i;
            }
        }
    }

    void updateGlobal () {
        int ind = 0;
        for (int i = 0; i < popSize; i++) {
            if (localFitness[ind] < localFitness[i]) {
                ind = i;
            }
        }
        X = population[ind];
    }

    void employed (vector <Agent> &agents) {
        for (int i = 0; i < popSize; i++) {
            double phi1 = randomDouble(-0.5, 0.5);
            double phi2 = randomDouble(0, 1.0);

            int eInd = randomInt(0, eliteSize - 1);
            double eX = population[elites[eInd]];

            int nAg = randomInt(0, (int) edges.size() - 1);
            int eJ = randomInt(0, eliteSize - 1);
            double eY = agents[edges[nAg].vid].population[elites[eJ]];

            double nX = agents[edges[nAg].vid].X;
            double mX = population[i];

            double now = 0.5 * (eY + X) + phi1 * (nX - mX) + phi2 * (nX - X);

            double nowUtil = singleUtil(now, agents);
            
            if (nowUtil > localFitness[i]) {
                population[i] = now;
                localFitness[i] = nowUtil;
            }
        }
    }

    void onlooker (vector <Agent> &agents) {
    }

    void scout () {
    }
};


double globalUtil (vector <Agent> &agents) {
    double ret = 0;
    for (Agent ai : agents) {
        ret += ai.localUtils(agents);
    }
    return ret / 2;
}

void ABCD (vector <Agent> &agents) {
    for (Agent &ai : agents) {
        ai.initialize();
    }

    int ITER = 100; // number of iterations
    double mainAns = DBL_MIN;
    for (int itr = 0; itr < ITER; itr++) {
        for (Agent &ai : agents) {
            ai.calcUtilities(agents);
            ai.build();

            ai.employed(agents);
            ai.onlooker(agents);
            ai.scout();

            ai.updateGlobal();
        }
        mainAns = max(mainAns, globalUtil(agents));
    }
}

int main() {
    vector <Agent> agents;

    int S = 100; // population size
    int M = 5; // elite set size
    int L = 5; // limit
    int UB = 50; // Upper Bound
    int LB = -50; // Lower Bound
    
    int nodes = 3, testNo = 0;
    vector <Edge> edges = get_test(nodes, testNo);
    for (int i = 0; i < nodes; i++) {
        agents.push_back(Agent(i, S, M, L, UB, LB));
    }

    for (Edge e : edges) {
        agents[e.uid].addEdge(e);
        swap(e.uid, e.vid);
        agents[e.uid].addEdge(e);
    }

    clock_t st = clock();
    
    ABCD(agents);

    clock_t en = clock();

    printf("Execution Time = %ld\n", (en - st) / CLOCKS_PER_SEC);
    return 0;
}

